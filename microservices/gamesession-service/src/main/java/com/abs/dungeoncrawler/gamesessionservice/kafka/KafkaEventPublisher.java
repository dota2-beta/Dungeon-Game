package com.abs.dungeoncrawler.gamesessionservice.kafka;

import com.abs.dungeonCrawler.eventcontracts.PlayerJoinedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPlayerJoinedEvent(PlayerJoinedEvent event) {
        kafkaTemplate.send("session-management-events", event);
    }
}
