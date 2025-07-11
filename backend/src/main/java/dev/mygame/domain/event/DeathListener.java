package dev.mygame.domain.event;

import dev.mygame.domain.model.Entity;

public interface DeathListener {
    void onEntityDied(Entity e);
}
