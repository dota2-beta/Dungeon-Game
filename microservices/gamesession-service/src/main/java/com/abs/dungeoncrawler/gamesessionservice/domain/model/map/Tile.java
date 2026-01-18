package com.abs.dungeoncrawler.gamesessionservice.domain.model.map;

import com.abs.dungeonCrawler.eventcontracts.enums.TileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Tile {
    private TileType type;
    private String occupiedById;

    // проходима ли клетка
    public boolean isPassable() {
        return this.type.isWalkable();
    }

    // занята ли клетка кем-то
    public boolean isOccupied() {
        return occupiedById != null;
    }
}
