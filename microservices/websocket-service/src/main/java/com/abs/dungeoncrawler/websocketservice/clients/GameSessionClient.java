package com.abs.dungeoncrawler.websocketservice.clients;

import com.dungeoncrawler.contracts.grpc.common.PlayerClassListResponse;
import com.dungeoncrawler.contracts.grpc.gamesession.*;
import com.google.protobuf.Empty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GameSessionClient {
    private final GameSessionServiceGrpc.GameSessionServiceBlockingStub gameSessionServiceBlockingStub;

    public GrpcJoinResponse joinSession(GrpcJoinRequest grpcRequest) {
        log.info("Sending JoinRequest to GameSessionService");
        try {
            return gameSessionServiceBlockingStub.joinSession(grpcRequest);
        } catch (Exception e) {
            log.error("Error while sending JoinRequest to GameSessionService", e);
            throw new RuntimeException("Failed to connect to GameSessionService. See cause.", e);
        }
    }
    public GrpcCreateSessionResponse createSession(GrpcCreateSessionRequest grpcRequest) {
        log.info("Sending CreateSessionRequest to GameSessionService");
        try {
            return gameSessionServiceBlockingStub.createSession(grpcRequest);
        }  catch (Exception e) {
            log.error("Error while sending CreateSessionRequest to GameSessionService", e);
            throw new RuntimeException("Failed to connect to GameSessionService. See cause.", e);
        }
    }

    public GrpcActionResponse moveRequest(GrpcMoveRequest grpcRequest) {
        log.info("Sending MoveRequest to GameSessionService");
        try {
            return gameSessionServiceBlockingStub.move(grpcRequest);
        } catch (Exception e) {
            log.error("Error while sending MoveRequest to GameSessionService", e);
            throw new RuntimeException("Failed to connect to GameSessionService. See cause.", e);
        }
    }

    public GrpcActionResponse attackRequest(GrpcAttackRequest grpcRequest) {
        log.info("Sending AttackRequest to GameSessionService");
        try {
            return gameSessionServiceBlockingStub.attack(grpcRequest);
        } catch (Exception e) {
            log.error("Error while sending AttackRequest to GameSessionService", e);
            throw new RuntimeException("Failed to connect to GameSessionService. See cause.", e);
        }
    }

    public GrpcActionResponse endTurnRequest(GrpcEndTurnRequest grpcRequest) {
        log.info("Sending EndTurnRequest to GameSessionService");
        try {
            return gameSessionServiceBlockingStub.endTurn(grpcRequest);
        } catch (Exception e) {
            log.error("Error while sending EndTurnRequest to GameSessionService", e);
            throw new RuntimeException("Failed to connect to GameSessionService. See cause.", e);
        }
    }

    public GetStateResponse getSessionState(String sessionId, String userId) {
        log.info("Sending GetStateRequest to GameSessionService");
        try {
            GetStateRequest request = GetStateRequest.newBuilder()
                    .setSessionId(sessionId)
                    .setUserId(userId)
                    .build();

            return gameSessionServiceBlockingStub.getSessionState(request);
        } catch (Exception e) {
            log.error("Error getting session state", e);
            throw new RuntimeException("gRPC call failed", e);
        }
    }

    public PlayerClassListResponse getPlayerClasses() {
        log.info("Sending PlayerClassesRequest to GameSessionService");
        try {
            return gameSessionServiceBlockingStub.getPlayerClasses(Empty.getDefaultInstance());
        } catch (Exception e) {
            log.error("Error getting player classes from GameSessionService", e);
            throw new RuntimeException("Failed to fetch player classes", e);
        }
    }

    public GrpcActionResponse proposePeace(GrpcProposePeaceRequest request) {
        log.info("Sending ProposePeaceRequest to GameSessionService");
        try {
            return gameSessionServiceBlockingStub.proposePeace(request);
        } catch (Exception e) {
            log.error("Error proposing peace", e);
            throw new RuntimeException("Failed to propose peace", e);
        }
    }

    public GrpcActionResponse respondToPeace(GrpcRespondToPeaceRequest request) {
        log.info("Sending RespondToPeaceRequest to GameSessionService");
        try {
            return gameSessionServiceBlockingStub.respondToPeace(request);
        } catch (Exception e) {
            log.error("Error responding peace", e);
            throw new RuntimeException("Failed to respond to peace", e);
        }
    }
}
