package com.abs.gamedataservice.mapper;

import com.abs.gamedataservice.data.templates.EntityClassTemplate;
import com.abs.gamedataservice.dto.GameDataLoadingDto;
import com.abs.gamedataservice.data.templates.EntityStatsTemplate;
import com.dungeoncrawler.contracts.grpc.common.Stats;
import com.dungeoncrawler.contracts.grpc.gamedata.EntityTemplateGrpcResponse;
import org.springframework.stereotype.Component;

@Component
public class EntityTemplateMapper {
    public EntityTemplateGrpcResponse mapToProto(EntityClassTemplate entity) {
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

        return EntityTemplateGrpcResponse.newBuilder()
                .setTemplateId(entity.getTemplateId())
                .setName(entity.getName())
                .setType(entity.getType())
                .setDescription(entity.getDescription())
                .addAllAbilities(entity.getAbilities())
                .setStats(protoStats)
                .build();
    }
    public EntityClassTemplate mapToEntityTemplate(GameDataLoadingDto.EntityTemplateDto dto) {
        EntityClassTemplate entity = EntityClassTemplate.builder()
                .templateId(dto.getTemplateId())
                .name(dto.getName())
                .type(dto.getType())
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
