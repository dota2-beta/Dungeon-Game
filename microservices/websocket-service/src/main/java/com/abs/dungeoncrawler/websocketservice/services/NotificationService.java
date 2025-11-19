package com.abs.dungeoncrawler.websocketservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final SimpMessagingTemplate simpMessagingTemplate;

    private record EventPayload (String eventType, Object payload) {}
    public void notifySessionWithEvent(String gameSessionId, String eventType, Object payload) {
        String destination = "/topic/session/" + gameSessionId + "/game-updates";
        EventPayload eventPayload = new EventPayload(eventType, payload);
        simpMessagingTemplate.convertAndSend(destination, eventPayload);
    }

    public void notifyUserWithError(String userId, String errorMessage) {
        simpMessagingTemplate.convertAndSendToUser(userId, "/queue/error", errorMessage);
    }
}
