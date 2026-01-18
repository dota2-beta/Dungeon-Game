package com.abs.dungeoncrawler.gamesessionservice.grpc;

import com.abs.dungeoncrawler.gamesessionservice.clients.GameDataClient;
import com.abs.dungeoncrawler.gamesessionservice.services.GameActionService;
import com.abs.dungeoncrawler.gamesessionservice.services.SessionLifecycleManager;
import com.dungeoncrawler.contracts.grpc.common.PlayerClassListResponse;
import com.dungeoncrawler.contracts.grpc.gamesession.*;
import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class GameSessionEndpoint extends GameSessionServiceGrpc.GameSessionServiceImplBase {
    private final SessionLifecycleManager sessionLifecycleManager;
    private final GameActionService gameActionService;
    private final GameDataClient gameDataClient;

    @Override
    public void joinSession(GrpcJoinRequest request, StreamObserver<GrpcJoinResponse> responseObserver) {
        log.info("GrpcJoinRequest successfully received");
        GrpcJoinResponse response = sessionLifecycleManager.handleJoinSession(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void createSession(GrpcCreateSessionRequest request, StreamObserver<GrpcCreateSessionResponse> responseObserver) {
        log.info("GrpcCreateSessionRequest successfully received");
        GrpcCreateSessionResponse response = sessionLifecycleManager.handleCreateSession(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void move(GrpcMoveRequest request, StreamObserver<GrpcActionResponse> responseObserver) {
        log.info("GrpcMoveRequest successfully received");
        GrpcActionResponse response = gameActionService.handleMoveAction(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void attack(GrpcAttackRequest request, StreamObserver<GrpcActionResponse> responseObserver) {
        log.info("GrpcAttackRequest successfully received");
        GrpcActionResponse response = gameActionService.handleAttackAction(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void endTurn(GrpcEndTurnRequest request, StreamObserver<GrpcActionResponse> responseObserver) {
        log.info("GrpcEndTurnRequest successfully received");
        GrpcActionResponse response = gameActionService.handleEndTurn(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getSessionState(GetStateRequest request, StreamObserver<GetStateResponse> responseObserver) {
        log.info("GrpcGetStateRequest successfully received");
        GetStateResponse response = gameActionService.handleGetGameSessionState(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getPlayerClasses(Empty request, StreamObserver<PlayerClassListResponse> responseObserver) {
        try {
            PlayerClassListResponse response = gameDataClient.getPlayerClasses();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to fetch player classes", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to fetch classes")
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    @Override
    public void proposePeace(GrpcProposePeaceRequest request, StreamObserver<GrpcActionResponse> responseObserver) {
        log.info("Received peace proposal from user {}", request.getUserId());
        GrpcActionResponse response = gameActionService.handleProposePeace(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void respondToPeace(GrpcRespondToPeaceRequest request, StreamObserver<GrpcActionResponse> responseObserver) {
        log.info("Received peace response from user {} (accept={})", request.getUserId(), request.getAccept());
        GrpcActionResponse response = gameActionService.handleRespondToPeace(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
