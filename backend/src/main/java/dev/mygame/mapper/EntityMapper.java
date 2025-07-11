package dev.mygame.mapper;

import dev.mygame.dto.websocket.response.PlayerState;
import dev.mygame.dto.websocket.response.EntityState;
import dev.mygame.dto.websocket.response.MonsterState;
import dev.mygame.domain.model.Entity;
import dev.mygame.domain.model.Monster;
import dev.mygame.domain.model.Player;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EntityMapper {
    @Mapping(target = "type", constant = "PLAYER")
    PlayerState toPlayerState(Player player);

    @Mapping(target = "monsterType", constant = "MONSTER")
    MonsterState toMonsterState(Monster monster);

    default EntityState toState(Entity entity) {
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
