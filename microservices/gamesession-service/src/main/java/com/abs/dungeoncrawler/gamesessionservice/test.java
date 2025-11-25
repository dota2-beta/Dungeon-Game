package com.abs.dungeoncrawler.gamesessionservice;

import com.abs.dungeoncrawler.gamesessionservice.clients.GameDataClient;
import com.dungeoncrawler.contracts.grpc.gamedata.PlayerTemplateGrpcResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Component
public class test implements CommandLineRunner {
    private final GameDataClient client;
    @Override
    public void run(String... args) throws Exception {
        Optional<PlayerTemplateGrpcResponse> response = client.getPlayerTemplate("warrior");
        if(response.isPresent()) {
            log.info(response.get().getName() + "\n" + response.get().getDescription());
        }
        else
            log.info("NOT FOUND TEMPLATE");
    }
}
