package dev.mygame.mapper;

import dev.mygame.dto.PlayerAction;
import dev.mygame.game.session.EntityAction;
import org.mapstruct.Mapper;

@Mapper
public interface EntityActionMapper {

    EntityAction toEntityAction (PlayerAction playerAction);
}
