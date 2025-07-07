package dev.mygame.mapper;

import dev.mygame.dto.EntityClientState;
import dev.mygame.dto.MonsterClientState;
import dev.mygame.dto.PlayerClientState;
import dev.mygame.game.model.Entity;
import dev.mygame.game.model.Monster;
import dev.mygame.game.model.Player;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EntityMapper {
    @Mapping(target = "type", constant = "PLAYER")
    PlayerClientState toPlayerClientState(Player player);

    @Mapping(target = "monsterType", constant = "MONSTER")
    MonsterClientState toMonsterClientState(Monster monster);

    default EntityClientState toClientState(Entity entity) {
        if (entity == null) {
            return null;
        }

        if (entity instanceof Player) {
            return toPlayerClientState((Player) entity);
        } else if (entity instanceof Monster) {
            return toMonsterClientState((Monster) entity);
        }

        throw new IllegalArgumentException("Unknown entity type: " + entity.getClass().getName());
    }
}
