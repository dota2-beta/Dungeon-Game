package dev.mygame.mapper;

import dev.mygame.dto.EntityClientState;
import dev.mygame.dto.GameSessionState;
import dev.mygame.dto.MapClientState;
import dev.mygame.game.enums.TileType;
import dev.mygame.game.model.Entity;
import dev.mygame.game.model.Player;
import dev.mygame.game.model.map.GameMap;
import dev.mygame.game.model.map.Point;
import dev.mygame.game.session.GameSession;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class GameSessionMapper {
    private final EntityMapper entityMapper;
    private final GameMapMapper gameMapMapper;

    public GameSessionState toGameSessionState(GameSession session, String forUserId) {
        GameSessionState gameSessionState = new GameSessionState();
        gameSessionState.setSessionId(session.getSessionID());

        String yourPlayerId = session.getEntities().values().stream()
                .filter(en -> en instanceof Player && ((Player) en).getUserId().equals(forUserId))
                .findFirst()
                .map(Entity::getId)
                .orElse(null);
        gameSessionState.setYourPlayerId(yourPlayerId);

        if(session.getGameMap() != null)
            gameSessionState.setMapState(gameMapMapper.toGameMapState(session.getGameMap()));

        List<EntityClientState> clientStates = session.getEntities().values().stream()
                .map(entityMapper::toEntityClientState)
                .toList();
        gameSessionState.setEntities(clientStates);

        return gameSessionState;
    }
}
