package dev.robertfuller.pollloggy.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class ObjectMapperConfig {
    @Bean
    public ObjectMapper getObjectMapper () {
        return new ObjectMapper();
    }
}
