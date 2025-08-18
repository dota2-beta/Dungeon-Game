package dev.mygame.enums;

import lombok.AllArgsConstructor;
import lombok.Generated;
import lombok.Getter;

/**
 * Определяет тип поверхности тайла на карте и его базовые физические свойства.
 */
@Getter
public enum TileType {
    FLOOR(true, false),
    WALL(false, false),
    PIT(true, false),
    DOOR(true, false),
    WATER(true, false);

    /** Если true, сущности могут перемещаться по этому тайлу. */
    private final boolean walkable;
    /** Если true, тайл может быть разрушен способностями или действиями. */
    private final boolean destructible;

    TileType(boolean walkable, boolean destructible) {
        this.walkable = walkable;
        this.destructible = destructible;
    }
}
