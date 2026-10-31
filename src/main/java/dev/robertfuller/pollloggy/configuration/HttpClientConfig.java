package dev.robertfuller.pollloggy.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;

@Configuration
public class HttpClientConfig {
    @Bean
    HttpClient getHttpClient () {
        return HttpClient.newHttpClient();
    }
}
