package com.abs.dungeoncrawler.websocketservice.config;

import com.dungeoncrawler.contracts.grpc.gamesession.GameSessionServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GrpcClientConfig {
    private final GrpcClientsProperties grpcClientsProperties;

    @Bean
    public GameSessionServiceGrpc.GameSessionServiceBlockingStub getGameSessionServiceBlockingStub() {
        String address = grpcClientsProperties.getClients().get("gamesession-service").getAddress();
        String[] parts = address.split(":");
        String host = parts[0];
        int port = Integer.parseInt(parts[1]);
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .build();
        return GameSessionServiceGrpc.newBlockingStub(channel);
    }
}
