package com.abs.dungeoncrawler.websocketservice;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.time.Duration;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
public class JoinSessionE2ETest {
    private static final Logger LOGGER = LoggerFactory.getLogger(JoinSessionE2ETest.class);

    public static Network network = Network.newNetwork();

//    @Autowired
//    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Container
    public static GenericContainer<?> kafka = new GenericContainer<>(DockerImageName.parse("confluentinc/cp-kafka:7.1.1"))
            .withEnv("KAFKA_PROCESS_ROLES", "broker,controller").withEnv("KAFKA_NODE_ID", "1")
            .withEnv("KAFKA_LISTENER_SECURITY_PROTOCOL_MAP", "CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT")
            .withEnv("KAFKA_ADVERTISED_LISTENERS", "PLAINTEXT://kafka:29092")
            .withEnv("KAFKA_CONTROLLER_QUORUM_VOTERS", "1@kafka:9093")
            .withEnv("KAFKA_LISTENERS", "PLAINTEXT://kafka:29092,CONTROLLER://kafka:9093")
            .withEnv("KAFKA_CONTROLLER_LISTENER_NAMES", "CONTROLLER")
            .withNetwork(network)
            .withExposedPorts(29092);

    // --- 3. Контейнер gamesession-service ---
    @Container
    public static GenericContainer<?> gamesessionService =
            new GenericContainer<>(DockerImageName.parse("dungeon-game/gamesession-service:0.0.1-SNAPSHOT"))
                    .withNetwork(network)
                    .withExposedPorts(9091)
                    .dependsOn(kafka)
                    .withEnv("SERVER_PORT", "9091")
                    .withEnv("GRPC_SERVER_PORT", "9091")
                    .withEnv("SPRING_KAFKA_PRODUCER_BOOTSTRAP_SERVERS", "kafka:29092") // Внутренний адрес
                    .waitingFor(Wait.forLogMessage(".*Started GamesessionServiceApplication.*\\n", 1)
                            .withStartupTimeout(Duration.ofMinutes(3)));
//
    @Container
    public static GenericContainer<?> websocketService =
            new GenericContainer<>(DockerImageName.parse("dungeon-game/websocket-service:0.0.1-SNAPSHOT"))
                    .withNetwork(network)
                    .withExposedPorts(9090)
                    .dependsOn(gamesessionService)
                    .withEnv("SERVER_PORT", "9090")
                    .withEnv("SPRING_KAFKA_CONSUMER_BOOTSTRAP_SERVERS", "kafka:29092") // Внутренний адрес
                    .withEnv("GRPC_CLIENTS_GAMESESSION-SERVICE_ADDRESS", "gamesession-service:9091")
                    .waitingFor(Wait.forLogMessage(".*Started WebsocketServiceApplication.*\\n", 1)
                            .withStartupTimeout(Duration.ofMinutes(3)));

    // Прикрепляем логгеры, чтобы видеть в консоли выводы от контейнеров. Очень полезно для отладки!
//    static {
//        gamesessionService.withLogConsumer(new Slf4jLogConsumer(LOGGER).withPrefix("GAMESESSION-SVC"));
//        websocketService.withLogConsumer(new Slf4jLogConsumer(LOGGER).withPrefix("WEBSOCKET-SVC"));
//        kafka.withLogConsumer(new Slf4jLogConsumer(LOGGER).withPrefix("KAFKA"));
//    }

    @Test
    void allContainersShouldStartSuccessfully() {
        System.out.println("ПОБЕДА! Все контейнеры успешно запущены и готовы к работе.");
    }
}
