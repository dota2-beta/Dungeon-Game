package dev.mygame.game.model.map;

import dev.mygame.game.enums.TileType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Tile {
    private TileType type;
    private String occupiedById;

    // проходима ли клетка
    public boolean isPassable() {
        return this.type.isWalkable() && !isOccupied();
    }

    // занята ли клетка
    public boolean isOccupied() {
        return occupiedById != null;
    }
}
