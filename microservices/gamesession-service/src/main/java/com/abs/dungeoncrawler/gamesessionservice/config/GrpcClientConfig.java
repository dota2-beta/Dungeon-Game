package com.abs.dungeoncrawler.gamesessionservice.config;

import com.dungeoncrawler.contracts.grpc.gamedata.GameDataServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@RequiredArgsConstructor
public class GrpcClientConfig {
    @Value("${grpc.clients.gamedata-service.host}")
    private String host;
    @Value("${grpc.clients.gamedata-service.port}")
    private int port;

    @Bean(destroyMethod = "shutdown")
    public ManagedChannel gameDataChannel() {
        return ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()//без ssl для локал
                .build();
    }
    @Bean
    public GameDataServiceGrpc.GameDataServiceBlockingStub gameDataServiceBlockingStub() {
        return GameDataServiceGrpc.newBlockingStub(gameDataChannel());
    }
}
