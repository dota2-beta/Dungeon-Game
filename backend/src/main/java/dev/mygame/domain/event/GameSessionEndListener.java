package dev.mygame.domain.event;

import dev.mygame.domain.session.GameSession;

public interface GameSessionEndListener {
    void onGameSessionEnd(GameSession session);
}
