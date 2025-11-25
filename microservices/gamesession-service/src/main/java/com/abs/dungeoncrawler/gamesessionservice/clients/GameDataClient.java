package com.abs.dungeoncrawler.gamesessionservice.clients;

import com.dungeoncrawler.contracts.grpc.gamedata.GameDataServiceGrpc;
import com.dungeoncrawler.contracts.grpc.gamedata.PlayerTemplateGrpcRequest;
import com.dungeoncrawler.contracts.grpc.gamedata.PlayerTemplateGrpcResponse;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
//import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameDataClient {
    private final GameDataServiceGrpc.GameDataServiceBlockingStub blockingStub;

    public Optional<PlayerTemplateGrpcResponse> getPlayerTemplate(String templateId) {
        try {
            PlayerTemplateGrpcRequest request = PlayerTemplateGrpcRequest.newBuilder()
                    .setTemplateId(templateId)
                    .build();
            PlayerTemplateGrpcResponse response = blockingStub.getPlayerTemplate(request);
            return Optional.ofNullable(response);
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                log.error("Player template not found");
                return Optional.empty();
            }
            log.error("Error calling GameDataService", e);
            throw e;
        }
    }
}
