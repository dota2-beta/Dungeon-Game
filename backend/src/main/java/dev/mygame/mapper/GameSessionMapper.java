package dev.mygame.mapper;

import dev.mygame.dto.websocket.response.EntityState;
import dev.mygame.dto.websocket.response.GameSessionState;
import dev.mygame.domain.model.Entity;
import dev.mygame.domain.model.Player;
import dev.mygame.domain.session.GameSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

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

        List<EntityState> clientStates = session.getEntities().values().stream()
                .map(entityMapper::toState)
                .filter(Objects::nonNull)
                .toList();
        gameSessionState.setEntities(clientStates);

        return gameSessionState;
    }
}
