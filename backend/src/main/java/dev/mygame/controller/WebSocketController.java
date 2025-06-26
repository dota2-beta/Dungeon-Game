package dev.mygame.controller;

import dev.mygame.dto.ChatMessageRequest;
import dev.mygame.dto.ChatMessageResponse;
import dev.mygame.dto.JoinRequestDto;
import dev.mygame.dto.PlayerAction;
import dev.mygame.game.session.GameSessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Date;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class WebSocketController {
    private final GameSessionManager gameSessionManager;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/join-session")
    public void onJoinSession(@Payload JoinRequestDto joinRequest, SimpMessageHeaderAccessor headerAccessor) {
        String websocketSessionId = headerAccessor.getSessionId();

        String sessionId = joinRequest.getSessionId();
        String userId = joinRequest.getUserId();

        try {
            gameSessionManager.joinPlayer(userId, sessionId, websocketSessionId);
        } catch (Exception e) {
            // ... отправить ошибку клиенту
        }
    }

    @MessageMapping("/session/{sessionId}/action")
    public void onPlayerAction(
            @DestinationVariable String sessionId,
            @Payload PlayerAction action,
            SimpMessageHeaderAccessor headerAccessor) {
        String websocketSessionId = headerAccessor.getSessionId();
        gameSessionManager.handlePlayerAction(sessionId, websocketSessionId, action);
    }

    @MessageMapping("/create-session") // Клиент отправляет сообщение на /app/create-session
    public void onCreateSession(SimpMessageHeaderAccessor headerAccessor) {
        String websocketSessionId = headerAccessor.getSessionId();
        // Временный userId, пока нет аутентификации
        String userId = websocketSessionId;

        try {
            // Вызываем упрощенный метод создания сессии в менеджере
            String sessionId = gameSessionManager.createSession();

            // Отправляем ID созданной сессии обратно клиенту
            Map<String, String> response = Map.of("sessionId", sessionId);
            messagingTemplate.convertAndSendToUser(userId, "/queue/session-created", response); // Используем личный топик
        } catch (Exception e) {
            // ... обработка ошибок ...
        }
    }

    @MessageMapping("/send/message")
    @SendTo("/topic/public")
    public ChatMessageResponse getMessage(@Payload ChatMessageRequest chatMessageRequest) {
        return new ChatMessageResponse(chatMessageRequest.getSender(), chatMessageRequest.getMessage(), new Date());
    }

}
