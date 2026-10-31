package dev.robertfuller.pollloggy.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EnvDataTests {
    @Test
    public void testEnvDataInstantiation () {
        float temp_f = 42.2F;
        float temp_c = 67.7F;
        float humidity = 73.3F;
        float pressure = 923.1F;

        EnvData subject = new EnvData(temp_f, temp_c, humidity, pressure);

        assertEquals(temp_f, subject.temp_f());
        assertEquals(temp_c, subject.temp_c());
        assertEquals(humidity, subject.humidity());
        assertEquals(pressure, subject.pressure());

    }
}
