package com.abs.dungeoncrawler.websocketservice.services;

import com.abs.dungeonCrawler.eventcontracts.Dto.GameSessionStateDto;
import com.abs.dungeonCrawler.eventcontracts.eventDto.SessionJoinedEvent;
import com.dungeoncrawler.contracts.grpc.common.PlayerClassTemplateMsg;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final SimpMessagingTemplate simpMessagingTemplate;

    public void notifySessionWithEvent(String gameSessionId, String eventType, Object payload) {
        String destination = "/topic/session/" + gameSessionId + "/game-updates";
        EventPayload eventPayload = new EventPayload(eventType, payload);
        simpMessagingTemplate.convertAndSend(destination, eventPayload);
    }

    public void notifyUserWithEvent(String userId, String eventType, Object payload) {
        String destination = "/queue/events";
        EventPayload eventPayload = new EventPayload(eventType, payload);
        simpMessagingTemplate.convertAndSendToUser(userId, destination, eventPayload);
    }

    public void sendError(String userId, String errorMessage, String errorCode) {
        String destination = "/queue/error";
        ErrorEvent error = new ErrorEvent(errorMessage, errorCode);
        simpMessagingTemplate.convertAndSendToUser(userId, destination, error);
    }

    public void sendStateSnapshot(String userId, GameSessionStateDto stateDto) {
        String destination = "/queue/state";
        EventPayload payload = new EventPayload("state_snapshot", stateDto);
        simpMessagingTemplate.convertAndSendToUser(userId, destination, payload);
    }

    public void sendSessionJoined(String userId, String sessionId) {
        String destination = "/queue/events";
        EventPayload payload = new EventPayload("session_joined", new SessionJoinedEvent(sessionId, userId));
        simpMessagingTemplate.convertAndSendToUser(userId, destination, payload);
    }

    public void sendPlayerClasses(String userId, List<PlayerClassTemplateMsg> protoClasses) {
        var classesDto = protoClasses.stream()
                .map(cls -> Map.of(
                        "templateId", cls.getTemplateId(),
                        "name", cls.getName()
                ))
                .toList();
        EventPayload payload = new EventPayload("classes_loaded", classesDto);
        simpMessagingTemplate.convertAndSendToUser(userId, "/queue/events", payload);
        log.info("Sent {} classes to user {}", classesDto.size(), userId);
    }

    private record EventPayload (String eventType, Object payload) {}
    public record ErrorEvent(String message, String errorCode) {}
}
