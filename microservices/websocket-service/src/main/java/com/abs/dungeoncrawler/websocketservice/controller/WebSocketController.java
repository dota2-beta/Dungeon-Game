package com.abs.dungeoncrawler.websocketservice.controller;

import com.abs.dungeoncrawler.websocketservice.dto.ProposePeaceRequestDto;
import com.abs.dungeoncrawler.websocketservice.dto.RespondToPeaceRequestDto;
import com.abs.dungeonCrawler.eventcontracts.requestDto.AttackRequestDto;
import com.abs.dungeonCrawler.eventcontracts.requestDto.EndTurnRequestDto;
import com.abs.dungeonCrawler.eventcontracts.requestDto.MoveRequestDto;
import com.abs.dungeoncrawler.websocketservice.ActionDispatcher;
import com.abs.dungeoncrawler.websocketservice.dto.CreateSessionRequestDto;
import com.abs.dungeoncrawler.websocketservice.dto.JoinRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
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
    public void onJoinSession(@Payload JoinRequestDto joinRequest,
                              SimpMessageHeaderAccessor headerAccessor,
                              Principal principal) {
        String websocketSessionId = headerAccessor.getSessionId();
        actionDispatcher.dispatchJoinSession(joinRequest, principal, websocketSessionId);
        //gameSessionManager.joinPlayer(joinRequest, userId, sessionId, websocketSessionId);
    }

    @MessageMapping("/create-session")
    public void onCreateSession(@Payload CreateSessionRequestDto createRequest,
                                SimpMessageHeaderAccessor headerAccessor,
                                Principal principal) {
        String websocketSessionId = headerAccessor.getSessionId();
        actionDispatcher.dispatchCreateSession(createRequest, principal, websocketSessionId);
    }

    @MessageMapping("/session/{sessionId}/move")
    public void onPlayerMoveAction(@Payload MoveRequestDto moveRequest,
                                   SimpMessageHeaderAccessor headerAccessor,
                                   Principal principal) {
        String websocketSessionId = headerAccessor.getSessionId();
        actionDispatcher.dispatchMoveRequest(moveRequest, principal, websocketSessionId);
    }

    @MessageMapping("/session/{sessionId}/attack")
    public void onPlayerAttackAction(@Payload AttackRequestDto attackRequest,
                                     SimpMessageHeaderAccessor headerAccessor,
                                     Principal principal) {
        String websocketSessionId = headerAccessor.getSessionId();
        actionDispatcher.dispatchAttackRequest(attackRequest, principal, websocketSessionId);
    }

    @MessageMapping("/session/{sessionId}/endTurn")
    public void onPlayerEndTurnAction(@Payload EndTurnRequestDto endTurnRequest,
                                      SimpMessageHeaderAccessor headerAccessor,
                                      Principal principal) {
        String websocketSessionId = headerAccessor.getSessionId();
        actionDispatcher.dispatchEndTurnRequest(endTurnRequest, principal, websocketSessionId);
    }

    @MessageMapping("/session/{sessionId}/state")
    public void onGetSessionState(@DestinationVariable String sessionId,
                                  Principal principal) {
        actionDispatcher.dispatchGetState(sessionId, principal.getName());
    }

    @MessageMapping("/get-classes")
    public void onGetClasses(Principal principal) {
        actionDispatcher.dispatchGetClasses(principal);
    }

    @MessageMapping("/session/{sessionId}/combat/propose-peace")
    public void onProposePeace(@Payload ProposePeaceRequestDto request,
                               SimpMessageHeaderAccessor headerAccessor,
                               Principal principal) {
        String websocketSessionId = headerAccessor.getSessionId();
        actionDispatcher.dispatchProposePeace(request.getSessionId(), principal.getName());
    }

    @MessageMapping("/session/{sessionId}/combat/respond-peace")
    public void onRespondToPeace(@Payload RespondToPeaceRequestDto request,
                                 Principal principal) {
        actionDispatcher.dispatchRespondToPeace(request.getSessionId(), principal.getName(), request.isAccept());
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
