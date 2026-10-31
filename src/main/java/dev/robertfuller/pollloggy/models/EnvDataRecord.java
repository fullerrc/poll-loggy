package dev.robertfuller.pollloggy.models;

import lombok.Builder;
import lombok.Value;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Value
@Builder
@Document
public class EnvDataRecord {
    @Id
    String id;
    float temp_c;
    float temp_f;
    float humidity;
    float pressure;
    @CreatedDate
    LocalDateTime createdAt;
    @LastModifiedDate
    LocalDateTime updatedAt;

    public static EnvDataRecord fromEnvData (EnvData envData) {
        return EnvDataRecord.builder()
                .temp_f(envData.temp_f())
                .temp_c(envData.temp_c())
                .humidity(envData.humidity())
                .pressure(envData.pressure())
                .build();
    }
}
