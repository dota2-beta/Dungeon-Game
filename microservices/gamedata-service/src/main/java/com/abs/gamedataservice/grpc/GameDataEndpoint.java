package com.abs.gamedataservice.grpc;

import com.abs.gamedataservice.data.MapLoader;
import com.abs.gamedataservice.data.templates.EntityClassTemplate;
import com.abs.gamedataservice.mapper.EntityTemplateMapper;
import com.abs.gamedataservice.service.TemplateService;
import com.dungeoncrawler.contracts.grpc.common.PlayerClassListResponse;
import com.dungeoncrawler.contracts.grpc.common.PlayerClassTemplateMsg;
import com.dungeoncrawler.contracts.grpc.gamedata.*;
import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.service.GrpcService;

import java.util.List;

@GrpcService
@Slf4j
@RequiredArgsConstructor
public class GameDataEndpoint extends GameDataServiceGrpc.GameDataServiceImplBase {
    private final EntityTemplateMapper mapper;
    private final TemplateService templateService;
    private final MapLoader mapLoader;

    @Override
    public void getEntityTemplate(EntityTemplateGrpcRequest request, StreamObserver<EntityTemplateGrpcResponse> responseObserver) {
        log.info("Received EntityTemplateRequest");
        templateService.getEntityTemplate(request.getTemplateId())
                .ifPresentOrElse(
                        template -> {
                            EntityTemplateGrpcResponse response = mapper.mapToProto(template);
                            responseObserver.onNext(response);
                            responseObserver.onCompleted();
                        },
                        () -> {
                            log.warn("Entity template not found");
                            responseObserver.onError(Status.NOT_FOUND
                                    .withDescription("Entity template with id " + request.getTemplateId() + " not found.")
                                    .asRuntimeException());
                        }
                );
    }

    @Override
    public void getGameMap(GameMapRequest request, StreamObserver<GameMapResponse> responseObserver) {
        log.info("Received GameMapRequest");
        GameMapResponse gameMap;
        try {
            //TODO: сейчас загрузка уровня захардкодена, надо будет исправить и заюзать GameMapRequest
            gameMap = mapLoader.loadMapFromFile("gamedata/maps/dungeon_level_1.txt");
            responseObserver.onNext(gameMap);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error loading gamemap", e);
            responseObserver.onError(Status.INTERNAL.withCause(e).asRuntimeException());
        }
        //подумать
    }

    @Override
    public void getPlayerClasses(Empty request, StreamObserver<PlayerClassListResponse> responseObserver) {
        log.info("Received PlayerClassesRequest");
        List<EntityClassTemplate> templates = templateService.getAllPlayerClasses();

        var list = templates.stream()
                .map(t -> PlayerClassTemplateMsg.newBuilder()
                        .setTemplateId(t.getTemplateId())
                        .setName(t.getName())
                        .build())
                .toList();

        responseObserver.onNext(PlayerClassListResponse.newBuilder().addAllClasses(list).build());
        responseObserver.onCompleted();
    }

}
