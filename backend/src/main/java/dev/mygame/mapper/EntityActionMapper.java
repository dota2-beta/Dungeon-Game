package dev.mygame.mapper;

import dev.mygame.dto.websocket.request.PlayerAction;
import dev.mygame.service.internal.EntityAction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EntityActionMapper {
    EntityAction toEntityAction(PlayerAction playerAction);
}
