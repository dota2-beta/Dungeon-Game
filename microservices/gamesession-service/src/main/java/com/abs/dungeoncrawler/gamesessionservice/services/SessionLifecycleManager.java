package com.abs.dungeoncrawler.gamesessionservice.services;

import com.abs.dungeonCrawler.eventcontracts.Dto.PlayerStateDto;
import com.abs.dungeonCrawler.eventcontracts.PlayerJoinedEvent;
import com.abs.dungeoncrawler.gamesessionservice.kafka.KafkaEventPublisher;
import com.dungeoncrawler.contracts.grpc.gamesession.JoinRequest;
import com.dungeoncrawler.contracts.grpc.gamesession.JoinResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SessionLifecycleManager {
    private final KafkaEventPublisher kafkaEventPublisher;

    public JoinResponse handleJoinSession(JoinRequest joinRequest) {
        log.info("Received player joined event: {}", joinRequest);
        PlayerStateDto dto = PlayerStateDto.builder()
                .userId(joinRequest.getUserId())
                .build();
        PlayerJoinedEvent event = PlayerJoinedEvent.builder()
                .sessionId(joinRequest.getSessionId())
                .playerStateDto(dto)
                .build();
        kafkaEventPublisher.publishPlayerJoinedEvent(event);
        return JoinResponse.newBuilder()
                .setSuccess(true)
                .build();
    }
}
