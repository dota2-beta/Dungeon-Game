package dev.mygame.mapper;

import dev.mygame.domain.session.AbilityInstance;
import dev.mygame.dto.websocket.response.AbilityCooldownDto;
import dev.mygame.dto.websocket.response.MonsterStateDto;
import dev.mygame.dto.websocket.response.PlayerStateDto;
import dev.mygame.dto.websocket.response.EntityStateDto;
import dev.mygame.domain.model.Entity;
import dev.mygame.domain.model.Monster;
import dev.mygame.domain.model.Player;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EntityMapper {
    @Mapping(source = "template.templateId", target = "abilityTemplateId")
    AbilityCooldownDto toAbilityCooldownDto(AbilityInstance instance);

    @Mapping(target = "type", constant = "PLAYER")
    @Mapping(source = "dead", target = "isDead")
    PlayerStateDto toPlayerState(Player player);

    @Mapping(target = "monsterType", constant = "MONSTER")
    @Mapping(source = "dead", target = "isDead")
    MonsterStateDto toMonsterState(Monster monster);

    default EntityStateDto toState(Entity entity) {
        if (entity == null) {
            return null;
        }

        if (entity instanceof Player) {
            return toPlayerState((Player) entity);
        } else if (entity instanceof Monster) {
            return toMonsterState((Monster) entity);
        }

        throw new IllegalArgumentException("Unknown entity type: " + entity.getClass().getName());
    }
}
