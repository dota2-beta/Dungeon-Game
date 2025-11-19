package com.abs.dungeoncrawler.websocketservice.clients;

import com.dungeoncrawler.contracts.grpc.gamesession.GameSessionServiceGrpc;
import com.dungeoncrawler.contracts.grpc.gamesession.JoinRequest;
import com.dungeoncrawler.contracts.grpc.gamesession.JoinResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GameSessionClient {
    private final GameSessionServiceGrpc.GameSessionServiceBlockingStub gameSessionServiceBlockingStub;

    public JoinResponse joinSession(JoinRequest grpcRequest) {
        log.info("Sending JoinRequest to GameSessionService");
        try {
            return gameSessionServiceBlockingStub.joinSession(grpcRequest);
        } catch (Exception e) {
            log.error("Error while sending JoinRequest to GameSessionService", e);
            throw new RuntimeException("Failed to connect to GameSessionService. See cause.", e);
        }
    }
}
