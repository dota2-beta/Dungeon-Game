package com.abs.dungeoncrawler.gamesessionservice.services;

import com.abs.dungeoncrawler.gamesessionservice.clients.GameDataClient;
import com.abs.dungeonCrawler.eventcontracts.enums.EntityState;
import com.abs.dungeoncrawler.gamesessionservice.domain.model.Monster;
import com.abs.dungeoncrawler.gamesessionservice.domain.model.Player;
import com.dungeoncrawler.contracts.grpc.common.Stats;
import com.dungeoncrawler.contracts.grpc.gamedata.EntityTemplateGrpcResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EntityFactory {
    private final GameDataClient gameDataClient;

    public Player createPlayer(String userId, String classTemplateId, String username) {
        var template = getTemplate(classTemplateId);
        var stats = template.getStats();

        return Player.builder()
                .id(UUID.randomUUID().toString())
                .name(username)
                .userId(userId)
                .teamId(UUID.randomUUID().toString())
                .maxHp(stats.getMaxHp())
                .currentHp(stats.getMaxHp())
                .attack(stats.getAttack())
                .defense(stats.getDefense())
                .maxAP(stats.getMaxAp())
                .currentAP(stats.getMaxAp())
                .initiative(stats.getInitiative())
                .attackRange(stats.getAttackRange())
                .aggroRadius(stats.getAggroRadius())
                .state(EntityState.EXPLORING)
                .build();
    }

    public Monster createMonster(String classTemplateId) {
        EntityTemplateGrpcResponse template = getTemplate(classTemplateId);
        Stats stats = template.getStats();

        return Monster.builder()
                .id(UUID.randomUUID().toString())
                .name(template.getName())
                .templateId(classTemplateId)
                .teamId("monsters") // У всех монстров одна команда (пока что)
                .maxHp(stats.getMaxHp())
                .currentHp(stats.getMaxHp())
                .attack(stats.getAttack())
                .defense(stats.getDefense())
                .maxAP(stats.getMaxAp())
                .currentAP(stats.getMaxAp())
                .initiative(stats.getInitiative())
                .attackRange(stats.getAttackRange())
                .aggroRadius(stats.getAggroRadius())
                .state(EntityState.EXPLORING)
                .build();
    }
    private EntityTemplateGrpcResponse getTemplate(String templateId) {
        return gameDataClient.getEntityTemplate(templateId)
                .orElseThrow(() -> new IllegalArgumentException("No template found: " + templateId));
    }
}
