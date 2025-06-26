package dev.mygame.game.session.event;

import dev.mygame.game.session.GameSession;

public interface GameSessionEndListener {
    void onGameSessionEnd(GameSession session);
}
