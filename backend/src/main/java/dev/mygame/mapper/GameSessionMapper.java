package dev.mygame.mapper;

import dev.mygame.dto.websocket.response.EntityStateDto;
import dev.mygame.dto.websocket.response.GameSessionStateDto;
import dev.mygame.domain.model.Entity;
import dev.mygame.domain.model.Player;
import dev.mygame.domain.session.GameSession;
import dev.mygame.mapper.context.MappingContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Component
public class GameSessionMapper {
    private final EntityMapper entityMapper;
    private final GameMapMapper gameMapMapper;

    public GameSessionStateDto toGameSessionState(GameSession session, MappingContext mappingContext) {
        GameSessionStateDto gameSessionStateDto = new GameSessionStateDto();
        gameSessionStateDto.setSessionId(session.getSessionID());

        String yourPlayerId = session.getEntities().values().stream()
                .filter(en -> en instanceof Player && ((Player) en).getUserId().equals(mappingContext.getForUserId()))
                .findFirst()
                .map(Entity::getId)
                .orElse(null);
        gameSessionStateDto.setYourPlayerId(yourPlayerId);

        if(session.getGameMap() != null)
            gameSessionStateDto.setMapState(gameMapMapper.toGameMapState(session.getGameMap(), mappingContext.getMapRadius()));

        List<EntityStateDto> clientStates = session.getEntities().values().stream()
                .map(entityMapper::toState)
                .filter(Objects::nonNull)
                .toList();
        gameSessionStateDto.setEntities(clientStates);

        return gameSessionStateDto;
    }
}
