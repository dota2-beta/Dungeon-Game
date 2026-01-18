package com.abs.dungeoncrawler.websocketservice;

import com.abs.dungeonCrawler.eventcontracts.Dto.GameSessionStateDto;
import com.abs.dungeonCrawler.eventcontracts.requestDto.AttackRequestDto;
import com.abs.dungeonCrawler.eventcontracts.requestDto.EndTurnRequestDto;
import com.abs.dungeonCrawler.eventcontracts.requestDto.MoveRequestDto;
import com.abs.dungeoncrawler.websocketservice.clients.GameSessionClient;
import com.abs.dungeoncrawler.websocketservice.dto.CreateSessionRequestDto;
import com.abs.dungeoncrawler.websocketservice.dto.JoinRequestDto;
import com.abs.dungeoncrawler.websocketservice.mapper.ProtoToDtoMapper;
import com.abs.dungeoncrawler.websocketservice.registry.ConnectionRegistry;
import com.abs.dungeoncrawler.websocketservice.services.NotificationService;
import com.dungeoncrawler.contracts.grpc.common.HexMsg;
import com.dungeoncrawler.contracts.grpc.gamesession.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
@RequiredArgsConstructor
@Slf4j
public class ActionDispatcher {
    private final GameSessionClient gameSessionClient;
    private final ConnectionRegistry connectionRegistry;
    private final ProtoToDtoMapper mapper;
    private final NotificationService notificationService;

    public void dispatchJoinSession(
            JoinRequestDto joinRequestDto,
            Principal principal,
            String webSocketSessionId
    ) {
        String username = joinRequestDto.getUsername();
        String sessionId = joinRequestDto.getSessionId();
        String userId;
        if (principal != null) {
            userId = principal.getName();
        } else {
            userId = "test-user-" + java.util.UUID.randomUUID();
        }
        log.info("Dispatching join request for user {} to session {}", username, sessionId);

        GrpcJoinRequest grpcRequest = GrpcJoinRequest.newBuilder()
                .setSessionId(sessionId)
                .setUsername(username)
                .setTemplateId(joinRequestDto.getTemplateId())
                .setUserId(userId)
                .build();
        try {
            GrpcJoinResponse response = gameSessionClient.joinSession(grpcRequest);
            if(response.getSuccess()) {
                log.info("User {} successfully connected to session {}", username, sessionId);
                connectionRegistry.register(webSocketSessionId, userId, sessionId);
                notificationService.sendSessionJoined(userId, sessionId);
            } else {
                log.error("User {} couldn't connect to session {}", username, sessionId);
                notificationService.sendError(userId,
                        response.getErrorMessage(),
                        "JOIN_SESSION_FAILED"
                );
            }
        } catch (RuntimeException e) {
            log.error("The call to GameSessionService failed. User will not be connected", e);
        }
    }

    public void dispatchCreateSession(
            CreateSessionRequestDto createRequest,
            Principal principal,
            String websocketSessionId) {
        String username = createRequest.getUsername();
        String userId;
        if (principal != null) {
            userId = principal.getName();
        } else {
            userId = "test-user-" + java.util.UUID.randomUUID();
        }
        log.info("Dispatching create request for user {}", username);

        GrpcCreateSessionRequest grpcRequest = GrpcCreateSessionRequest.newBuilder()
                .setUsername(username)
                .setTemplateId(createRequest.getTemplateId())
                .setUserId(userId)
                .setLevel(createRequest.getLevel())
                .build();

        try {
            GrpcCreateSessionResponse response = gameSessionClient.createSession(grpcRequest);
            if(response.getSuccess()) {
                log.info("User {} successfully create session", username);
                // вроде это не надо
                //connectionRegistry.register(websocketSessionId, userId, response.getSessionId());
            } else {
                log.error("Something went wrong and user {} couldn't register in session {}",
                        username,
                        response.getSessionId());
                notificationService.sendError(userId,
                        response.getErrorMessage(),
                        "CREATE_SESSION_FAILED");
            }
        } catch (RuntimeException e) {
            log.error("The call to GameSessionService failed. User will not be connected", e);
        }
    }

    public void dispatchMoveRequest(@Payload MoveRequestDto moveRequest,
                                    Principal principal,
                                    String websocketSessionId) {
        String sessionId = moveRequest.getSessionId();
        HexMsg targetHex = HexMsg.newBuilder()
                        .setQ(moveRequest.getTarget().getQ())
                        .setR(moveRequest.getTarget().getR())
                        .build();
        String userId;
        if (principal != null) {
            userId = principal.getName();
        } else {
            userId = "test-user-" + java.util.UUID.randomUUID();
        }
        log.info("Dispatching move request for user {}", userId);

        GrpcMoveRequest grpcRequest = GrpcMoveRequest.newBuilder()
                .setSessionId(sessionId)
                .setCoordinate(targetHex)
                .setUserId(userId)
                .build();
        try {
            GrpcActionResponse response = gameSessionClient.moveRequest(grpcRequest);
            if(response.getSuccess()) {
                log.info("User {} successfully moved in session {}", userId, sessionId);
            } else {
                log.warn("Move failed for user {}: {}", userId, response.getErrorMessage());

                notificationService.sendError(
                        userId,
                        response.getErrorMessage(),
                        "ACTION_FAILED"
                );
            }
        } catch (RuntimeException e) {
            log.error("The call to GameSessionService failed. User will not be connected", e);
        }
    }

    public void dispatchAttackRequest(AttackRequestDto attackRequest, Principal principal, String websocketSessionId) {
        String sessionId = attackRequest.getSessionId();
        String userId;
        if (principal != null) {
            userId = principal.getName();
        } else {
            userId = "test-user-" + java.util.UUID.randomUUID();
        }
        log.info("Dispatching attack request for user {}", userId);

        GrpcAttackRequest grpcRequest = GrpcAttackRequest.newBuilder()
                .setSessionId(sessionId)
                .setAttackerId(attackRequest.getAttackerId())
                .setTargetId(attackRequest.getTargetId())
                .build();
        try {
            GrpcActionResponse response = gameSessionClient.attackRequest(grpcRequest);
            if(response.getSuccess()) {
                log.info("User {} successfully attacked target {} in session {}",
                        userId,
                        attackRequest.getTargetId(),
                        sessionId
                );
            } else {
                log.error("Something went wrong ang user {} cannot attack", userId);
                notificationService.sendError(
                        userId,
                        response.getErrorMessage(),
                        "ACTION_FAILED"
                );
            }
        } catch (RuntimeException e) {
            log.error("The call to GameSessionService failed. User will not be connected", e);
        }
    }

    public void dispatchEndTurnRequest(EndTurnRequestDto endTurnRequest, Principal principal, String websocketSessionId) {
        String sessionId = endTurnRequest.getSessionId();
        String userId;
        if (principal != null) {
            userId = principal.getName();
        } else {
            userId = "test-user-" + java.util.UUID.randomUUID();
        }
        log.info("Dispatching endTurn request for user {}", userId);

        GrpcEndTurnRequest grpcRequest = GrpcEndTurnRequest.newBuilder()
                .setSessionId(sessionId)
                .setUserId(userId)
                .build();
        try {
            GrpcActionResponse response = gameSessionClient.endTurnRequest(grpcRequest);
            if(response.getSuccess()) {
                log.info("User {} successfully ended turn in session {}",
                        userId,
                        sessionId
                );
            } else {
                log.error("Something went wrong ang user {} cannot end turn", userId);
                notificationService.sendError(
                        userId,
                        response.getErrorMessage(),
                        "ACTION_FAILED"
                );
            }
        } catch (RuntimeException e) {
            log.error("The call to GameSessionService failed. User will not be connected", e);
        }
    }

    public void dispatchGetState(String sessionId, String userId) {
        try {
            GetStateResponse response = gameSessionClient.getSessionState(sessionId, userId);

            if (response.getSuccess()) {
                GameSessionStateDto stateDto = mapper.toDto(response.getState());

                notificationService.sendStateSnapshot(userId, stateDto);

            } else {
                // notificationService.sendError(userId, ...);
            }
        } catch (Exception e) {
            log.error("...", e);
        }
    }

    public void dispatchProposePeace(String sessionId, String userId) {
        GrpcProposePeaceRequest grpcRequest = GrpcProposePeaceRequest.newBuilder()
                .setSessionId(sessionId)
                .setUserId(userId)
                .build();

        try {
            GrpcActionResponse response = gameSessionClient.proposePeace(grpcRequest);
            if (!response.getSuccess()) {
                notificationService.sendError(userId, response.getErrorMessage(), "PEACE_PROPOSAL_FAILED");
            }
        } catch (Exception e) {
            log.error("Failed to dispatch peace proposal", e);
        }
    }

    public void dispatchRespondToPeace(String sessionId, String userId, boolean accept) {
        GrpcRespondToPeaceRequest grpcRequest = GrpcRespondToPeaceRequest.newBuilder()
                .setSessionId(sessionId)
                .setUserId(userId)
                .setAccept(accept)
                .build();

        try {
            GrpcActionResponse response = gameSessionClient.respondToPeace(grpcRequest);
            if (!response.getSuccess()) {
                notificationService.sendError(userId, response.getErrorMessage(), "PEACE_RESPONSE_FAILED");
            }
        } catch (Exception e) {
            log.error("Failed to dispatch peace response", e);
        }
    }

    public void dispatchGetClasses(Principal principal) {
        String userId = principal.getName();
        var response = gameSessionClient.getPlayerClasses();
        notificationService.sendPlayerClasses(userId, response.getClassesList());
    }
}
