package dev.mygame.mapper;

import dev.mygame.dto.websocket.response.MapState;
import dev.mygame.enums.TileType;
import dev.mygame.domain.model.map.GameMap;
import dev.mygame.domain.model.map.Point;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GameMapMapper {
    public MapState toGameMapState(GameMap gameMap) {
        MapState mapClientState = new MapState();
        mapClientState.setWidth(gameMap.getWidth());
        mapClientState.setHeight(gameMap.getHeight());

        List<TileType> tileTypes = new ArrayList<>();
        for(int j = 0; j < gameMap.getHeight(); j++) {
            for(int i = 0; i < gameMap.getWidth(); i++) {
                tileTypes.add(gameMap.getTile(new Point(i, j)).getType());
            }
        }
        mapClientState.setTiles(tileTypes);

        List<Point> spawnPoints = new ArrayList<>();
        spawnPoints.add(new Point(0, 0));

        mapClientState.setSpawnPoints(spawnPoints);
        return mapClientState;
    }
}
