package dev.mygame.mapper;

import dev.mygame.dto.PlayerAction;
import dev.mygame.game.session.EntityAction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EntityActionMapper {
    EntityAction toEntityAction(PlayerAction playerAction);
}
