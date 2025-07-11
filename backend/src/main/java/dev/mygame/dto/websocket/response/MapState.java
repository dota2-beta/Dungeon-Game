package dev.mygame.dto.websocket.response;

import dev.mygame.enums.TileType;
import dev.mygame.domain.model.map.Point;
import lombok.Data;

import java.util.List;

@Data
public class MapState {
    public int height;
    public int width;
    List<TileType> tiles;
    List<Point> spawnPoints;
}
