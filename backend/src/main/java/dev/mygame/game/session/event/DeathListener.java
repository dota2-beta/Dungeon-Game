package dev.mygame.game.session.event;

import dev.mygame.game.model.Entity;

public interface DeathListener {
    void onEntityDied(Entity e);
}
