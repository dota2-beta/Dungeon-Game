package dev.mygame.dto;

import dev.mygame.game.enums.TileType;
import dev.mygame.game.model.map.Point;
import lombok.Data;

import java.util.List;

@Data
public class MapClientState {
    public int height;
    public int width;
    List<TileType> tiles;
    List<Point> spawnPoints;
}
