package dev.mygame.game.enums;

import lombok.AllArgsConstructor;
import lombok.Generated;
import lombok.Getter;

@Getter
public enum TileType {
    // Type(walkable, destructible)
    FLOOR(true, false),
    WALL(true, false),
    PIT(true, false),
    DOOR(true, false),
    WATER(true, false);

    private final boolean walkable;
    private final boolean destructible;

    TileType(boolean walkable, boolean destructible) {
        this.walkable = walkable;
        this.destructible = destructible;
    }

}
