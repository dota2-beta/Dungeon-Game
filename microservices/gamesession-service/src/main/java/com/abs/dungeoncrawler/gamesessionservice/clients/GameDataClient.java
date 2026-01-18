package com.abs.dungeoncrawler.gamesessionservice.clients;

import com.dungeoncrawler.contracts.grpc.common.PlayerClassListResponse;
import com.dungeoncrawler.contracts.grpc.gamedata.*;
import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameDataClient {
    private final GameDataServiceGrpc.GameDataServiceBlockingStub blockingStub;

    public Optional<EntityTemplateGrpcResponse> getEntityTemplate(String templateId) {
        try {
            EntityTemplateGrpcRequest request = EntityTemplateGrpcRequest.newBuilder()
                    .setTemplateId(templateId)
                    .build();
            return Optional.ofNullable(blockingStub.getEntityTemplate(request));
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                log.error("Player template not found");
                return Optional.empty();
            }
            log.error("Error calling GameDataService", e);
            throw e;
        }
    }

    public Optional<GameMapResponse> getGameMap(String level) {
        try {
            GameMapRequest request = GameMapRequest.newBuilder()
                    .setLevel(level)
                    .build();
            return Optional.ofNullable(blockingStub.getGameMap(request));
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                log.error("Game map not found");
                return Optional.empty();
            }
            log.error("Error calling GameDataService", e);
            throw e;
        }
    }

    public PlayerClassListResponse getPlayerClasses() {
        try {
            return blockingStub.getPlayerClasses(Empty.getDefaultInstance());
        } catch (Exception e) {
            log.error("Error calling GameDataService getPlayerClasses", e);
            throw e;
        }
    }
}
