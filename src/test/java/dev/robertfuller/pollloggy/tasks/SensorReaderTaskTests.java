package dev.robertfuller.pollloggy.tasks;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import dev.robertfuller.pollloggy.models.EnvData;
import dev.robertfuller.pollloggy.models.EnvDataRecord;
import dev.robertfuller.pollloggy.repositories.EnvDataRepository;
import dev.robertfuller.pollloggy.services.SensorClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SensorReaderTaskTests {
    @Mock
    SensorClient mockSensorClient;
    @Mock
    EnvDataRepository mockEnvDataRepository;
    @Mock
    EnvData mockEnvData;
    @InjectMocks
    SensorReaderTask subject;

    final Logger taskLogger = (Logger) LoggerFactory.getLogger(SensorReaderTask.class);
    final ListAppender<ILoggingEvent> logAppender = new ListAppender<>();
    Level originalLevel;

    @BeforeEach
    void attachLogAppender() {
        originalLevel = taskLogger.getLevel();
        taskLogger.setLevel(Level.DEBUG);
        logAppender.start();
        taskLogger.addAppender(logAppender);
    }

    @AfterEach
    void detachLogAppender() {
        taskLogger.detachAppender(logAppender);
        taskLogger.setLevel(originalLevel);
    }

    @Test
    public void testReadAndReportSensor() throws Exception {
        when(mockSensorClient.readSensor()).thenReturn(mockEnvData);

        subject.readAndReportSensor();

        verify(mockSensorClient).readSensor();
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getLevel, ILoggingEvent::getFormattedMessage)
                .containsExactly(tuple(Level.DEBUG, mockEnvData.toString()));

        verify(mockEnvDataRepository).save(EnvDataRecord.fromEnvData(mockEnvData));
    }
}
