package com.abs.dungeoncrawler.websocketservice;

import com.abs.dungeonCrawler.eventcontracts.Dto.PlayerStateDto;
import com.abs.dungeonCrawler.eventcontracts.PlayerJoinedEvent;
import com.abs.dungeoncrawler.websocketservice.services.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@ExtendWith(MockitoExtension.class)
public class JoinSessionE2ETest {
    @Container
    static final KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("apache/kafka-native:3.8.0")
    );
    @DynamicPropertySource
    static void overrideKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.consumer.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.producer.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.producer.value-serializer", () -> "org.springframework.kafka.support.serializer.JsonSerializer");
    }

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void whenPlayerJoinedEventIsReceived_thenNotificationServiceIsCalled() {
        //arrange
        PlayerStateDto dto = PlayerStateDto.builder()
                .userId("testId")
                .build();
        PlayerJoinedEvent event = PlayerJoinedEvent.builder()
                .playerStateDto(dto)
                .sessionId("testSessionId")
                .build();
        //act
        kafkaTemplate.send("session-management-events", event);
        //assert
        ArgumentCaptor<PlayerJoinedEvent> eventCaptor = ArgumentCaptor.forClass(PlayerJoinedEvent.class);

        verify(notificationService, timeout(5000).times(1))
                .notifySessionWithEvent(
                        eq("testSessionId"),
                        eq("player_joined"),
                        eventCaptor.capture()
                );

        PlayerJoinedEvent capturedEvent = eventCaptor.getValue();
        assertEquals("testId", capturedEvent.getPlayerStateDto().getUserId());
    }
}
