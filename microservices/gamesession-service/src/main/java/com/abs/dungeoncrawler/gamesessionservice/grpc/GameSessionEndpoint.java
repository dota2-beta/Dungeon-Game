package com.abs.dungeoncrawler.gamesessionservice.grpc;

import com.abs.dungeoncrawler.gamesessionservice.services.SessionLifecycleManager;
import com.dungeoncrawler.contracts.grpc.gamesession.GameSessionServiceGrpc;
import com.dungeoncrawler.contracts.grpc.gamesession.JoinRequest;
import com.dungeoncrawler.contracts.grpc.gamesession.JoinResponse;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class GameSessionEndpoint extends GameSessionServiceGrpc.GameSessionServiceImplBase {
    private final SessionLifecycleManager sessionLifecycleManager;

    @Override
    public void joinSession(JoinRequest request, StreamObserver<JoinResponse> responseObserver) {
        log.info("Request successfully received");
        JoinResponse response = sessionLifecycleManager.handleJoinSession(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
