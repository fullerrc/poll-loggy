package dev.robertfuller.pollloggy.models;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EnvDataRecordTests {
    @Test
    public void testFromEnvData() {
        float temp_f = 42.2F;
        float temp_c = 67.7F;
        float humidity = 73.3F;
        float pressure = 923.1F;

        EnvData envData = new EnvData(temp_f, temp_c, humidity, pressure);

        EnvDataRecord subject = EnvDataRecord.fromEnvData(envData);

        assertEquals(temp_f, subject.getTemp_f());
        assertEquals(temp_c, subject.getTemp_c());
        assertEquals(humidity, subject.getHumidity());
        assertEquals(pressure, subject.getPressure());
    }

    @Test
    public void testEnvDataRecordInstantiation() {
        String id = "abc123";
        float temp_f = 42.2F;
        float temp_c = 67.7F;
        float humidity = 73.3F;
        float pressure = 923.1F;
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();

        EnvDataRecord subject =
                EnvDataRecord.builder()
                        .id(id)
                        .temp_c(temp_c)
                        .temp_f(temp_f)
                        .humidity(humidity)
                        .pressure(pressure)
                        .createdAt(createdAt)
                        .updatedAt(updatedAt)
                        .build();

        assertEquals(id, subject.getId());
        assertEquals(temp_f, subject.getTemp_f());
        assertEquals(temp_c, subject.getTemp_c());
        assertEquals(humidity, subject.getHumidity());
        assertEquals(pressure, subject.getPressure());
        assertEquals(createdAt, subject.getCreatedAt());
        assertEquals(updatedAt, subject.getUpdatedAt());
    }
}
