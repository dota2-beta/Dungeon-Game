package com.abs.dungeoncrawler.websocketservice.config;

import com.dungeoncrawler.contracts.grpc.gamesession.GameSessionServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GrpcClientConfig {
    @Value("${grpc.clients.gamesession-service.host}")
    private String host;
    @Value("${grpc.clients.gamesession-service.port}")
    private String port;

    @Bean
    public GameSessionServiceGrpc.GameSessionServiceBlockingStub getGameSessionServiceBlockingStub() {
        int port = Integer.parseInt(this.port);
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .build();
        return GameSessionServiceGrpc.newBlockingStub(channel);
    }
}
