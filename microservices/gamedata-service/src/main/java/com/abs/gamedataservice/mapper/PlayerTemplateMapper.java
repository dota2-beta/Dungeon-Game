package com.abs.gamedataservice.mapper;

import com.abs.gamedataservice.dto.GameDataLoadingDto;
import com.abs.gamedataservice.templates.EntityStatsTemplate;
import com.abs.gamedataservice.templates.PlayerClassTemplate;
import com.dungeoncrawler.contracts.grpc.gamedata.PlayerTemplateGrpcResponse;
import com.dungeoncrawler.contracts.grpc.gamedata.Stats;
import org.springframework.stereotype.Component;

@Component
public class PlayerTemplateMapper {
    public PlayerTemplateGrpcResponse mapToProto(PlayerClassTemplate entity) {
        EntityStatsTemplate stats = entity.getStats();

        Stats protoStats = Stats.newBuilder()
                .setMaxHp(stats.getMaxHp())
                .setAttack(stats.getAttack())
                .setDefense(stats.getDefense())
                .setInitiative(stats.getInitiative())
                .setMaxAp(stats.getMaxAP())
                .setAttackRange(stats.getAttackRange())
                .setAggroRadius(stats.getAggroRadius())
                .build();

        return PlayerTemplateGrpcResponse.newBuilder()
                .setTemplateId(entity.getTemplateId())
                .setName(entity.getName())
                .setDescription(entity.getDescription())
                .addAllAbilities(entity.getAbilities())
                .setStats(protoStats)
                .build();
    }
    public PlayerClassTemplate mapToPlayerTemplate(GameDataLoadingDto.PlayerTemplateDto dto) {
        PlayerClassTemplate entity = PlayerClassTemplate.builder()
                .templateId(dto.getTemplateId())
                .name(dto.getName())
                .description(dto.getDescription())
                .abilities(dto.getAbilities())
                .build();
        EntityStatsTemplate stats = EntityStatsTemplate.builder()
                .maxHp(dto.getStats().getMaxHp())
                .maxAP(dto.getStats().getMaxAP())
                .attack(dto.getStats().getAttack())
                .attackRange(dto.getStats().getAttackRange())
                .defense(dto.getStats().getDefense())
                .aggroRadius(dto.getStats().getAggroRadius())
                .initiative(dto.getStats().getInitiative())
                .build();
        entity.setStats(stats);
        return entity;
    }
}
