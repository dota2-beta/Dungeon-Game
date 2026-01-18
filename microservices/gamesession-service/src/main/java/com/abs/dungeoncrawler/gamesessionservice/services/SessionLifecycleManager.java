package com.abs.dungeoncrawler.gamesessionservice.services;

import com.abs.dungeonCrawler.eventcontracts.Dto.EntityStateDto;
import com.abs.dungeonCrawler.eventcontracts.EntityJoinedEvent;
import com.abs.dungeonCrawler.eventcontracts.SessionCreatedEvent;
import com.abs.dungeoncrawler.gamesessionservice.domain.GameSession;
import com.abs.dungeoncrawler.gamesessionservice.domain.model.Entity;
import com.abs.dungeoncrawler.gamesessionservice.domain.model.Monster;
import com.abs.dungeoncrawler.gamesessionservice.domain.model.Player;
import com.abs.dungeoncrawler.gamesessionservice.domain.model.map.Hex;
import com.abs.dungeoncrawler.gamesessionservice.exception.GameActionException;
import com.abs.dungeoncrawler.gamesessionservice.exception.SessionNotFoundException;
import com.abs.dungeoncrawler.gamesessionservice.kafka.KafkaEventPublisher;
import com.abs.dungeoncrawler.gamesessionservice.mapper.EntityMapper;
import com.abs.dungeoncrawler.gamesessionservice.mapper.GameMapMapper;
import com.dungeoncrawler.contracts.grpc.gamesession.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SessionLifecycleManager {
    private final KafkaEventPublisher kafkaEventPublisher;
    private final CoreGameEngine coreGameEngine;
    private final EntityFactory entityFactory;
    private final EntityMapper entityMapper;
    private final SessionRepository sessionRepository;

    public GrpcJoinResponse handleJoinSession(GrpcJoinRequest joinRequest) {
        try {
            log.info("Received player joined event: {}", joinRequest);
            GameSession gameSession = sessionRepository.findById(joinRequest.getSessionId()).orElseThrow();
            Player player = entityFactory.createPlayer(
                    joinRequest.getUserId(),
                    joinRequest.getTemplateId(),
                    joinRequest.getUsername()
            );
            addEntityToGameSession(gameSession, player);
            return GrpcJoinResponse.newBuilder()
                    .setSuccess(true)
                    .build();
        } catch (Exception e) {
            log.error("Failed to join session", e);
            return GrpcJoinResponse.newBuilder()
                    .setSuccess(false)
                    .build();
        }
    }
    public GrpcCreateSessionResponse handleCreateSession(GrpcCreateSessionRequest request) {
        try {
            log.info("Received create session event: {}", request);
            String sessionId = UUID.randomUUID().toString();
            GameSession gameSession = coreGameEngine.createSession(sessionId);
            Player player = entityFactory.createPlayer(
                    request.getUserId(),
                    request.getTemplateId(),
                    request.getUsername()
            );
            addEntityToGameSession(gameSession, player);
            sessionRepository.save(gameSession);

            SessionCreatedEvent event = SessionCreatedEvent.builder()
                            .sessionId(sessionId)
                    .userId(request.getUserId())
                            .build();
            kafkaEventPublisher.publishSessionCreatedEvent(event);
            return GrpcCreateSessionResponse.newBuilder()
                    .setSuccess(true)
                    .setSessionId(gameSession.getSessionId())
                    .build();
        } catch (Exception e) {
            log.error("Failed to join session", e);
            return GrpcCreateSessionResponse.newBuilder()
                    .setSuccess(false)
                    .setErrorMessage(e.getMessage())
                    .build();
        }
    }

    private void addEntityToGameSession(GameSession gameSession, Entity entity) {
        gameSession.addEntity(entity);
        Hex spawnPoint;
        if(entity instanceof Player) {
            spawnPoint = gameSession.getGameMap().getAvailablePlayerSpawnPoint();
        } else {
            List<Hex> spawnPoints = gameSession.getGameMap()
                    .getAvailableMonsterSpawnPoints(((Monster) entity).getTemplateId());
            spawnPoint = spawnPoints.stream()
                    .filter(point -> !gameSession.getGameMap().getTile(point).isOccupied())
                    .findFirst().orElseThrow();
        }
        entity.setPosition(spawnPoint);
        EntityStateDto dto = entityMapper.toEntityState(entity);

        EntityJoinedEvent event = EntityJoinedEvent.builder()
                .sessionId(gameSession.getSessionId())
                .entityStateDto(dto)
                .build();
        kafkaEventPublisher.publishEntityJoinedEvent(event);
    }
}
