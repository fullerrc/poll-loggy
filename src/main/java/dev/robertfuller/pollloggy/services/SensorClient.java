package dev.robertfuller.pollloggy.services;

import dev.robertfuller.pollloggy.models.EnvData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
@RequiredArgsConstructor
public class SensorClient {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${config.sensorModuleUri}")
    private String sensorModuleUri;

    public EnvData readSensor () throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(sensorModuleUri))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readValue(response.body(), EnvData.class);
    }
}
