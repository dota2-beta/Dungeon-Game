package com.abs.dungeoncrawler.gamesessionservice.services;

import com.abs.dungeoncrawler.gamesessionservice.domain.GameSession;
import com.abs.dungeoncrawler.gamesessionservice.domain.model.Player;
import com.abs.dungeoncrawler.gamesessionservice.exception.GameActionException;
import com.abs.dungeoncrawler.gamesessionservice.exception.SessionNotFoundException;
import com.abs.dungeoncrawler.gamesessionservice.mapper.GameMapMapper;
import com.abs.dungeoncrawler.gamesessionservice.mapper.GameSessionMapper;
import com.dungeoncrawler.contracts.grpc.gamesession.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GameActionService {
    private final SessionRepository sessionRepository;
    private final CoreGameEngine coreGameEngine;
    private final GameMapMapper gameMapMapper;
    private final CombatService combatService;
    private final GameSessionMapper gameSessionMapper;

    public GrpcActionResponse handleMoveAction(GrpcMoveRequest request) {
        try {
            log.info("Processing move for user {} in session {}", request.getUserId(), request.getSessionId());
            GameSession gameSession = sessionRepository.findById(request.getSessionId())
                    .orElseThrow(() -> new SessionNotFoundException("Session " + request.getSessionId() + " not found"));

            Player player = gameSession.getPlayerByUserId(request.getUserId())
                    .orElseThrow(() -> new GameActionException("Player not found", "PLAYER_NOT_FOUND"));
            coreGameEngine.handleMoveAction(
                    gameSession,
                    player.getId(),
                    gameMapMapper.grpcHexMsgToHex(request.getCoordinate())
            );

            return GrpcActionResponse.newBuilder()
                    .setSuccess(true)
                    .build();

        } catch (GameActionException e) {
            log.warn("Move rejected: {}", e.getMessage());
            return GrpcActionResponse.newBuilder()
                    .setSuccess(false)
                    .setErrorMessage(e.getMessage())
                    .build();

        } catch (Exception e) {
            log.error("System error during move", e);
            return GrpcActionResponse.newBuilder()
                    .setSuccess(false)
                    .setErrorMessage("Internal Server Error")
                    .build();
        }
    }

    public GrpcActionResponse handleEndTurn(GrpcEndTurnRequest request) {
        try {
            GameSession session = sessionRepository.findById(request.getSessionId())
                    .orElseThrow(() ->
                            new SessionNotFoundException("Session " + request.getSessionId() + " not found"));

            Player player = session.getPlayerByUserId(request.getUserId())
                    .orElseThrow(() -> new GameActionException("Player not found", "PLAYER_NOT_FOUND"));

            combatService.requestEndTurn(session, player.getId());

            return GrpcActionResponse.newBuilder().setSuccess(true).build();

        } catch (GameActionException e) {
            log.warn("End Turn rejected: {}", e.getMessage());
            return GrpcActionResponse.newBuilder()
                    .setSuccess(false)
                    .setErrorMessage(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("System error during end turn", e);
            return GrpcActionResponse.newBuilder()
                    .setSuccess(false)
                    .setErrorMessage(e.getMessage())
                    .build();
        }
    }

    public GrpcActionResponse handleAttackAction(GrpcAttackRequest request) {
        try {
            log.info("Processing attack for user {} per user {} in session {}",
                    request.getAttackerId(),
                    request.getTargetId(),
                    request.getSessionId()
            );
            GameSession gameSession = sessionRepository.findById(request.getSessionId())
                    .orElseThrow(() -> new SessionNotFoundException("Session " + request.getSessionId() + " not found"));
            coreGameEngine.handleAttackAction(
                    gameSession,
                    request.getAttackerId(),
                    request.getTargetId()
            );
            return GrpcActionResponse.newBuilder()
                    .setSuccess(true)
                    .build();

        } catch (GameActionException e) {
            log.warn("Attack rejected: {}", e.getMessage());
            return GrpcActionResponse.newBuilder()
                    .setSuccess(false)
                    .setErrorMessage(e.getMessage())
                    .build();

        } catch (Exception e) {
            log.error("System error during attack", e);
            return GrpcActionResponse.newBuilder()
                    .setSuccess(false)
                    .setErrorMessage("Internal Server Error")
                    .build();
        }
    }

    public GetStateResponse handleGetGameSessionState(GetStateRequest request) {
        try {
            GameSession session = sessionRepository.findById(request.getSessionId())
                    .orElseThrow(() -> new SessionNotFoundException("Session not found"));

            GameSessionStateMsg stateMsg = gameSessionMapper.toProto(session);

            return GetStateResponse.newBuilder()
                    .setSuccess(true)
                    .setState(stateMsg)
                    .build();

        } catch (Exception e) {
            log.error("Error getting session state", e);

            return GetStateResponse.newBuilder()
                    .setSuccess(false)
                    .setErrorMessage(e.getMessage())
                    .build();
        }
    }

    public GrpcActionResponse handleProposePeace(GrpcProposePeaceRequest request) {
        try {
            GameSession session = sessionRepository.findById(request.getSessionId())
                    .orElseThrow(() -> new SessionNotFoundException("Session not found"));

            combatService.proposePeace(session, request.getUserId());

            return GrpcActionResponse.newBuilder().setSuccess(true).build();

        } catch (GameActionException e) {
            log.warn("Peace proposal rejected: {}", e.getMessage());
            return GrpcActionResponse.newBuilder().setSuccess(false).setErrorMessage(e.getMessage()).build();
        } catch (Exception e) {
            log.error("Error proposing peace", e);
            return GrpcActionResponse.newBuilder().setSuccess(false).setErrorMessage("Internal Error").build();
        }
    }

    public GrpcActionResponse handleRespondToPeace(GrpcRespondToPeaceRequest request) {
        try {
            GameSession session = sessionRepository.findById(request.getSessionId())
                    .orElseThrow(() -> new SessionNotFoundException("Session not found"));

            combatService.respondToPeace(session, request.getUserId(), request.getAccept());

            return GrpcActionResponse.newBuilder().setSuccess(true).build();

        } catch (GameActionException e) {
            log.warn("Peace response rejected: {}", e.getMessage());
            return GrpcActionResponse.newBuilder().setSuccess(false).setErrorMessage(e.getMessage()).build();
        } catch (Exception e) {
            log.error("Error responding to peace", e);
            return GrpcActionResponse.newBuilder().setSuccess(false).setErrorMessage("Internal Error").build();
        }
    }
}
