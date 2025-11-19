package com.abs.dungeoncrawler.websocketservice.controller;

import com.abs.dungeoncrawler.websocketservice.ActionDispatcher;
import com.abs.dungeoncrawler.websocketservice.dto.JoinRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {
    private final ActionDispatcher actionDispatcher;

    @MessageMapping("/join-session")
    public void onJoinSession(@Payload JoinRequestDto joinRequest, SimpMessageHeaderAccessor headerAccessor, Principal principal) {
        String websocketSessionId = headerAccessor.getSessionId();
        actionDispatcher.dispatchJoinSession(joinRequest, principal, websocketSessionId);
        //gameSessionManager.joinPlayer(joinRequest, userId, sessionId, websocketSessionId);
    }

//    @MessageMapping("/session/{sessionId}/action")
//    public void onPlayerAction(
//            @DestinationVariable String sessionId,
//            @Payload PlayerAction action,
//            SimpMessageHeaderAccessor headerAccessor) {
//        String websocketSessionId = headerAccessor.getSessionId();
//        gameSessionManager.handlePlayerAction(sessionId, websocketSessionId, action);
//    }
//
//    @MessageMapping("/create-session")
//    public void onCreateSession(Principal principal) {
//        String userId = principal.getName();
//
//        try {
//            String sessionId = gameSessionManager.createSession();
//
//            Map<String, String> response = Map.of(
//                    "status", "success",
//                    "sessionId", sessionId
//            );
//            messagingTemplate.convertAndSendToUser(userId, "/queue/create-session-response", response);
//        } catch (Exception e) {
//            log.error("Error creating game session for user: {}", userId, e);
//
//            Map<String, String> errorResponse = Map.of(
//                    "status", "error",
//                    "message", "Failed to create session: " + e.getMessage()
//            );
//            messagingTemplate.convertAndSendToUser(userId, "/queue/create-session-response", errorResponse);
//        }
//    }
//
//    @MessageMapping("/session/{sessionId}/team/invite")
//    public void onInviteToTeam(
//            @DestinationVariable String sessionId,
//            @Payload InviteToTeamRequest request,
//            Principal principal
//    ) {
//        String currentUserId = principal.getName();
//        gameSessionManager.invitePlayerToTeam(sessionId, currentUserId, request.getTargetPlayerId());
//    }
//
//    @MessageMapping("/session/{sessionId}/team/respond")
//    public void onRespondToTeam(
//            @DestinationVariable String sessionId,
//            @Payload RespondToTeamRequest request,
//            Principal principal
//    ) {
//        String currentUserId = principal.getName();
//        gameSessionManager.respondPlayerToTeamInvite(sessionId, currentUserId, request.isAccepted());
//    }
//
//    @MessageMapping("/session/{sessionId}/team/leave")
//    public void onLeaveFromTeam(
//            @DestinationVariable String sessionId,
//            Principal principal
//    ) {
//        String currentUserId = principal.getName();
//        gameSessionManager.leaveFromTeam(sessionId, currentUserId);
//    }
//
//    @MessageMapping("/session/{sessionId}/combat/{combatId}/propose-peace")
//    public void onProposePeace(
//            @DestinationVariable String sessionId,
//            @DestinationVariable String combatId,
//            Principal principal
//    ) {
//        gameSessionManager.handlePeaceProposal(sessionId, principal.getName(), combatId);
//    }
//
//    @MessageMapping("/session/{sessionId}/combat/{combatId}/respond-peace")
//    public void onRespondToPeace(
//            @DestinationVariable String sessionId,
//            @DestinationVariable String combatId,
//            Principal principal,
//            @Payload RespondToPeaceRequest request
//    ) {
//        gameSessionManager.handlePeaceResponse(sessionId, principal.getName(), combatId, request.isAccepted());
//    }

    /**
     * Обрабатывает запрос от клиента на полную ресинхронизацию состояния.
     * Срабатывает, когда игрок возвращается в активную вкладку.
     */
//    @MessageMapping("/session/{sessionId}/request-state")
//    public void onRequestFullState(
//            @DestinationVariable String sessionId,
//            Principal principal
//    ) {
//        String userId = principal.getName();
//        log.info("User {} requested full state sync for session {}", userId, sessionId);
//        gameSessionManager.resendStateToPlayer(sessionId, userId);
//    }
}
