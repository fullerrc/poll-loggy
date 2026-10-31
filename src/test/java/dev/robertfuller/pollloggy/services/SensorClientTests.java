package dev.robertfuller.pollloggy.services;

import dev.robertfuller.pollloggy.models.EnvData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SensorClientTests {
    @Mock
    HttpClient mockHttpClient;
    @Mock
    ObjectMapper mockObjectMapper;
    SensorClient subject;
    @Mock
    HttpResponse<String> mockResponse;

    @BeforeEach
    void setUp () {
        subject = new SensorClient(
                mockHttpClient,
                mockObjectMapper);
        ReflectionTestUtils.setField(
                subject, "sensorModuleUri", "https://example.com");
    }

    @Test
    public void testReadSensor () {
        HttpRequest expectedRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://example.com"))
                .build();
        assertDoesNotThrow(() -> {
            when(mockHttpClient.send(
                    expectedRequest, HttpResponse.BodyHandlers.ofString())).thenReturn(mockResponse);
            subject.readSensor();
            verify(
                    mockHttpClient,
                    times(1)).send(
                        expectedRequest, HttpResponse.BodyHandlers.ofString());
            verify(
                    mockObjectMapper,
                    times(1)).readValue(mockResponse.body(), EnvData.class);
            verify(mockResponse, times(2)).body();
        });

    }
}
