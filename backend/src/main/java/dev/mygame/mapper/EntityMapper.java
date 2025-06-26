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

    @Mapping(target = "type", constant = "ENTITY")
    EntityClientState toEntityClientState(Entity entity);
}
