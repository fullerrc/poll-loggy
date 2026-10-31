# syntax=docker/dockerfile:1
FROM eclipse-temurin:25-jdk-jammy AS builder
ARG MAVEN_VERSION=3.9.16
ARG SHA=831a8591fe20c8243b1dbe7d71e3244f31d1665b0804b2e825e38cbbe5ce0cafb8338851f90780735568773e0a6cd07bbec107cda0b896b008b861075358b6f6
ARG BASE_URL=https://downloads.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries

RUN apt-get update && \
    apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /usr/share/maven /usr/share/maven/ref \
  && curl -fsSL -o /tmp/apache-maven.tar.gz ${BASE_URL}/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
  && echo "${SHA}  /tmp/apache-maven.tar.gz" | sha512sum -c - \
  && tar -xzf /tmp/apache-maven.tar.gz -C /usr/share/maven --strip-components=1 \
  && rm -f /tmp/apache-maven.tar.gz \
  && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

COPY . .

RUN mvn clean package -Dmaven.test.skip=true

FROM gcr.io/distroless/java21-debian12

COPY --from=builder /target/poll-loggy-0.0.1-SNAPSHOT.jar /app/poll-loggy-0.0.1-SNAPSHOT.jar

ENTRYPOINT ["java", "-jar", "/app/poll-loggy-0.0.1-SNAPSHOT.jar"]
