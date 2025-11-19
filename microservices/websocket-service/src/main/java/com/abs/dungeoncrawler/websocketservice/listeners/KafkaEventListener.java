package com.abs.dungeoncrawler.websocketservice.listeners;

import com.abs.dungeonCrawler.eventcontracts.PlayerJoinedEvent;
import com.abs.dungeoncrawler.websocketservice.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaEventListener {
    private final NotificationService notificationService;

    @KafkaListener(topics = "session-management-events",
            groupId = "websocket-consumers")
    public void handlePlayerJoinedEvent (PlayerJoinedEvent e) {
        notificationService.notifySessionWithEvent(
                e.getSessionId(),
                "player_joined",
                e
        );
    }
}
