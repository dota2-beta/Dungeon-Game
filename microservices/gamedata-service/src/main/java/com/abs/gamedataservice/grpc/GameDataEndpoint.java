package com.abs.gamedataservice.grpc;

import com.abs.gamedataservice.mapper.PlayerTemplateMapper;
import com.abs.gamedataservice.repositories.PlayerTemplateRepository;
import com.abs.gamedataservice.service.TemplateService;
import com.dungeoncrawler.contracts.grpc.gamedata.GameDataServiceGrpc;
import com.dungeoncrawler.contracts.grpc.gamedata.PlayerTemplateGrpcRequest;
import com.dungeoncrawler.contracts.grpc.gamedata.PlayerTemplateGrpcResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.service.GrpcService;

import static org.springframework.data.util.Optionals.ifPresentOrElse;

@GrpcService
@Slf4j
@RequiredArgsConstructor
public class GameDataEndpoint extends GameDataServiceGrpc.GameDataServiceImplBase {
    private final PlayerTemplateMapper mapper;
    private final TemplateService templateService;

    @Override
    public void getPlayerTemplate(PlayerTemplateGrpcRequest request, StreamObserver<PlayerTemplateGrpcResponse> responseObserver) {
        log.info("Received PlayerTemplateRequest");
        //здесь надо будет всё поменять
        templateService.getPlayerTemplate(request.getTemplateId())
                .ifPresentOrElse(
                        template -> {
                            PlayerTemplateGrpcResponse response = mapper.mapToProto(template);
                            responseObserver.onNext(response);
                            responseObserver.onCompleted();
                        },
                        () -> {
                            log.warn("Player template not found");
                            responseObserver.onError(Status.NOT_FOUND
                                    .withDescription("Player template with id " + request.getTemplateId() + " not found.")
                                    .asRuntimeException());
                        }
                );
    }
}
