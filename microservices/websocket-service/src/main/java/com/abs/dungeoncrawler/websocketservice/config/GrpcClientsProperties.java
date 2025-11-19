package com.abs.dungeoncrawler.websocketservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@Data
@ConfigurationProperties(prefix = "grpc")
public class GrpcClientsProperties {
    private Map<String, ClientProperties> clients;

    @Data
    public static class ClientProperties {
        private String address;
    }
}
