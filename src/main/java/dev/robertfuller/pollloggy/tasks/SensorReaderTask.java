package dev.robertfuller.pollloggy.tasks;

import dev.robertfuller.pollloggy.models.EnvData;
import dev.robertfuller.pollloggy.models.EnvDataRecord;
import dev.robertfuller.pollloggy.repositories.EnvDataRepository;
import dev.robertfuller.pollloggy.services.SensorClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class SensorReaderTask {
    private final SensorClient sensorClient;
    private final EnvDataRepository envDataRepository;

    @Scheduled(fixedRate = 5000)
    public void readAndReportSensor () throws IOException, InterruptedException {
        EnvData envData = sensorClient.readSensor();
        log.debug(envData.toString());
        envDataRepository.save(EnvDataRecord.fromEnvData(envData));
    }
}
