package dev.mygame.controller.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.mygame.dto.websocket.request.*;
import dev.mygame.service.GameSessionManager;
import dev.mygame.service.internal.EntityAction;
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
    private final ObjectMapper objectMapper;

    @MessageMapping("/join-session")
    public void onJoinSession(@Payload JoinRequest joinRequest, SimpMessageHeaderAccessor headerAccessor, Principal principal) {
        String websocketSessionId = headerAccessor.getSessionId();

        String sessionId = joinRequest.getSessionId();
        String userId = principal.getName();

        try {
            gameSessionManager.joinPlayer(joinRequest, userId, sessionId, websocketSessionId);
        } catch (Exception e) {
            log.error("Error joining player {} to session {}:", userId, sessionId, e);
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
        String userId = principal.getName();

        try {
            String sessionId = gameSessionManager.createSession();

            Map<String, String> response = Map.of(
                    "status", "success",
                    "sessionId", sessionId
            );
            messagingTemplate.convertAndSendToUser(userId, "/queue/create-session-response", response);
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

    @MessageMapping("/session/{sessionId}/team/leave")
    public void onLeaveFromTeam(
            @DestinationVariable String sessionId,
            Principal principal
    ) {
        String currentUserId = principal.getName();
        gameSessionManager.leaveFromTeam(sessionId, currentUserId);
    }

    @MessageMapping("/session/{sessionId}/combat/{combatId}/propose-peace")
    public void onProposePeace(
            @DestinationVariable String sessionId,
            @DestinationVariable String combatId,
            Principal principal
    ) {
        gameSessionManager.handlePeaceProposal(sessionId, principal.getName(), combatId);
    }

    @MessageMapping("/session/{sessionId}/combat/{combatId}/respond-peace")
    public void onRespondToPeace(
            @DestinationVariable String sessionId,
            @DestinationVariable String combatId,
            Principal principal,
            @Payload RespondToPeaceRequest request
    ) {
        gameSessionManager.handlePeaceResponse(sessionId, principal.getName(), combatId, request.isAccepted());
    }
}
