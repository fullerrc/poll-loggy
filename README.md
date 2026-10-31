# PollLoggy

PollLoggy is a small, general-purpose **polling and action server** built on Spring Boot. It runs scheduled
tasks. Each task polls a data source, acts on the result, and records it.

Its current task is **environmental monitoring**. Every 5 seconds PollLoggy polls an atmospheric sensor module
over HTTP for temperature, humidity and barometric pressure, then saves each reading to MongoDB with
automatic timestamps.

```
┌──────────────────┐   HTTP GET (every 5s)   ┌───────────────────────────────┐   save   ┌──────────┐
│  Sensor module   │ ◀────────────────────── │           PollLoggy           │ ───────▶ │ MongoDB  │
│  (JSON endpoint) │ ──────────────────────▶ │ SensorReaderTask → SensorClient│          │          │
└──────────────────┘   {temp_f, temp_c, …}   └───────────────────────────────┘          └──────────┘
```

## Tech stack

| Area          | Choice                                                              |
|---------------|---------------------------------------------------------------------|
| Language      | Java 21                                                             |
| Framework     | Spring Boot 4.1 (scheduling, Spring Data MongoDB)                   |
| HTTP client   | JDK `java.net.http.HttpClient` (no third-party HTTP dependency)     |
| JSON          | Jackson 3 (`tools.jackson`)                                         |
| Persistence   | MongoDB through Spring Data repositories, with auditing             |
| Boilerplate   | Lombok (`@Value`, `@Builder`, `@RequiredArgsConstructor`, `@Slf4j`) |
| Testing       | JUnit 5, Mockito (including static mocking), AssertJ, Testcontainers|
| Packaging     | Multi-stage Docker build, distroless runtime image                  |

## How it works

| Component          | Package          | Responsibility                                                                                 |
|--------------------|------------------|------------------------------------------------------------------------------------------------|
| `SensorReaderTask` | `tasks`          | `@Scheduled(fixedRate = 5000)` entry point: reads the sensor, logs the reading, saves it        |
| `SensorClient`     | `services`       | Sends a GET to the configured sensor URI and deserializes the JSON body into `EnvData`          |
| `EnvData`          | `models`         | Immutable Java `record` that matches the sensor's wire format                                  |
| `EnvDataRecord`    | `models`         | Immutable MongoDB `@Document`, with `@CreatedDate` / `@LastModifiedDate` audit fields          |
| `EnvDataRepository`| `repositories`   | Spring Data `MongoRepository` for `EnvDataRecord`                                              |
| `*Config`          | `configuration`  | Turns on scheduling and Mongo auditing; provides the `HttpClient` and `ObjectMapper` beans     |

The wire model (`EnvData`) and the persistence model (`EnvDataRecord`) are separate classes, joined by
`EnvDataRecord.fromEnvData(...)`. A change to the sensor's payload therefore doesn't change the stored schema,
and a change to the stored schema doesn't change the payload. Dependencies use constructor injection, so every
component can be built in a plain unit test without a Spring context.

### Sensor contract

PollLoggy expects the sensor endpoint to return a JSON object like this:

```json
{ "temp_f": 72.4, "temp_c": 22.4, "humidity": 41.2, "pressure": 1013.2 }
```

### Adding a new poll-and-act task

The pattern is designed to repeat:

1. Add a client in `services/` that fetches or produces the data.
2. Add models, plus a repository if the results should be stored.
3. Add a `@Component` in `tasks/` with a `@Scheduled` method that ties them together.

Scheduling is already turned on application-wide (`SchedulingConfig`), so you don't need any other setup.

## Configuration

| Property                 | Environment variable       | Description                                                   |
|--------------------------|----------------------------|---------------------------------------------------------------|
| `spring.mongodb.uri`     | `SPRING_MONGODB_URI`       | MongoDB connection string, e.g. `mongodb://host:27017/pollloggy` |
| `config.sensorModuleUri` | `CONFIG_SENSORMODULEURI`   | Full URL of the sensor module's JSON endpoint                 |

The `local` profile (`application-local.yaml`) sets the `dev.robertfuller.pollloggy` logger to `DEBUG`. With
`DEBUG` on, every reading is logged as it's received.

## Running

### Prerequisites

- JDK 21 or newer. The Maven wrapper (`./mvnw`) is included.
- A reachable MongoDB, or Docker to run one through Testcontainers.
- A sensor module that serves the JSON shown above.

### Locally, against your own MongoDB

```bash
SPRING_MONGODB_URI=mongodb://localhost:27017/pollloggy \
CONFIG_SENSORMODULEURI=http://<sensor-host>/<path> \
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Locally, with a throwaway MongoDB (Testcontainers)

`TestPollLoggyApplication` starts the real application wired to a MongoDB container that Testcontainers
creates. You don't need to install or configure a database:

```bash
./mvnw spring-boot:test-run \
  -Dspring-boot.run.profiles=local \
  -Dspring-boot.run.arguments=--config.sensorModuleUri=http://<sensor-host>/<path>
```

### Docker

```bash
docker build -t poll-loggy .
docker run --rm \
  -e SPRING_MONGODB_URI=mongodb://<mongo-host>:27017/pollloggy \
  -e CONFIG_SENSORMODULEURI=http://<sensor-host>/<path> \
  poll-loggy
```

The Dockerfile uses a multi-stage build:

- **Build stage:** downloads Maven and **verifies it against a pinned SHA-512 checksum** before using it to
  package the jar.
- **Runtime stage:** `gcr.io/distroless/java21-debian12`, an image with no shell or package manager. This keeps
  the image small and its attack surface low.
- `.dockerignore` leaves tests, IDE files and build output out of the build context.

## Testing approach

The test suite works in two layers. Fast, isolated unit tests cover all of the application's logic. A
container-backed integration test proves that the whole application starts against a real database.

```bash
./mvnw test
```

### Unit tests: each class in isolation

The unit tests use **Mockito** with **constructor injection**, so each class is tested without a Spring
context, network or database:

- **`SensorClientTests`**: mocks `HttpClient`, `HttpResponse` and `ObjectMapper`. It checks that the client
  builds the request for the configured URI and passes the response body to the deserializer.
- **`SensorReaderTaskTests`**: mocks the client and the repository. It checks the whole poll → log → save
  sequence. To test the logging, it attaches a Logback **`ListAppender`** to the task's logger and asserts
  that exactly one `DEBUG` event with the expected message was logged. Afterwards it restores the original
  log level, so the test doesn't affect other tests.
- **`EnvDataTests` / `EnvDataRecordTests`**: cover the record accessors, the Lombok builder and the
  `fromEnvData` mapping between the wire model and the persistence model.
- **`PollLoggyApplicationUnitTests`**: tests even the `main` method. It uses Mockito's **static mocking**
  (`mockStatic(SpringApplication.class)`) to check that `main` passes its arguments to
  `SpringApplication.run`. It doesn't start the application.

### Integration test: the real context and a real MongoDB

- **`PollLoggyApplicationTests`** starts the full Spring application context. It uses a real `mongo` container
  that Testcontainers manages and that `@ServiceConnection` wires in. This catches configuration and wiring
  problems that mocks can't catch, such as auditing setup, repository creation and bean graph errors.
- The same `TestcontainersConfiguration` also supports the `spring-boot:test-run` workflow above. Tests and
  local development get their database the same way.

### Build setup for modern JDKs

Recent JDKs warn about agents that attach themselves at runtime, and Mockito's inline mock maker is one of
them. The build prepares for this:

- The Surefire plugin attaches Mockito as an explicit `-javaagent`.
- `maven-dependency-plugin:properties` resolves the path to the Mockito jar.

As a result, static and final-class mocking works on current JDKs without warnings.

### Docker runtime notes (Colima and others)

The integration test needs a running Docker-compatible runtime. Testcontainers looks for the socket at
`/var/run/docker.sock` by default. If you use **Colima**, point Testcontainers at Colima's socket instead:

```bash
export DOCKER_HOST="unix://$HOME/.colima/default/docker.sock"
export TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock
./mvnw test
```

## Project layout

```
src/main/java/dev/robertfuller/pollloggy/
├── PollLoggyApplication.java
├── configuration/   # HttpClient, ObjectMapper, scheduling, Mongo auditing
├── models/          # EnvData (wire) and EnvDataRecord (persisted)
├── repositories/    # Spring Data MongoDB repositories
├── services/        # Outbound clients (SensorClient)
└── tasks/           # @Scheduled poll-and-act jobs (SensorReaderTask)
src/test/java/...    # Mirrors main/, plus Testcontainers configuration
```

## Roadmap

- Make the polling interval configurable instead of fixed at 5 seconds.
- Handle failures explicitly: non-2xx responses, timeouts, and retry or backoff for an unreachable sensor.
- Add read endpoints for querying stored readings. Spring Web MVC is already a dependency.
- Add CI to run the unit and integration tests on every push.
