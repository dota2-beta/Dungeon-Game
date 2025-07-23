package dev.mygame.controller.websocket;

import dev.mygame.dto.websocket.request.InviteToTeamRequest;
import dev.mygame.dto.websocket.request.JoinRequest;
import dev.mygame.dto.websocket.request.PlayerAction;
import dev.mygame.dto.websocket.request.RespondToTeamRequest;
import dev.mygame.service.GameSessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class WebSocketController {
    private static final Logger log = LoggerFactory.getLogger(WebSocketController.class);

    private final GameSessionManager gameSessionManager;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/join-session")
    public void onJoinSession(@Payload JoinRequest joinRequest, SimpMessageHeaderAccessor headerAccessor, Principal principal) {
        String websocketSessionId = headerAccessor.getSessionId();

        String sessionId = joinRequest.getSessionId();
        String userId = principal.getName();

        try {
            gameSessionManager.joinPlayer(userId, sessionId, websocketSessionId);
        } catch (Exception e) {
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

    @MessageMapping("/create-session")
    public void onCreateSession(SimpMessageHeaderAccessor headerAccessor, Principal principal) {
        String websocketSessionId = headerAccessor.getSessionId();

        //временный userId пока нет аутентификации
        String userId = principal.getName();
        //String userId = websocketSessionId;

        try {
            String sessionId = gameSessionManager.createSession();

            Map<String, String> response = Map.of(
                    "status", "success",
                    "sessionId", sessionId
            );
            messagingTemplate.convertAndSendToUser(userId, "/queue/create-session-response", response); // Используем личный топик
        } catch (Exception e) {
            log.error("Error creating game session for user: {}", userId, e);

            Map<String, String> errorResponse = Map.of(
                    "status", "error",
                    "message", "Failed to create session: " + e.getMessage()
            );
            messagingTemplate.convertAndSendToUser(userId, "/queue/create-session-response", errorResponse);
        }
    }

    @MessageMapping("/session/{sessionId}/team/invite")
    public void onInviteToTeam(
            @DestinationVariable String sessionId,
            @Payload InviteToTeamRequest request,
            Principal principal
    ) {
        String currentUserId = principal.getName();
        gameSessionManager.invitePlayerToTeam(sessionId, currentUserId, request.getTargetPlayerId());
    }

    @MessageMapping("/session/{sessionId}/team/respond")
    public void onRespondToTeam(
            @DestinationVariable String sessionId,
            @Payload RespondToTeamRequest request,
            Principal principal
    ) {
        String currentUserId = principal.getName();
        gameSessionManager.respondPlayerToTeamInvite(sessionId, currentUserId, request.isAccepted());
    }
}
