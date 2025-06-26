package dev.mygame.mapper;

import dev.mygame.dto.MapClientState;
import dev.mygame.game.enums.TileType;
import dev.mygame.game.model.map.GameMap;
import dev.mygame.game.model.map.Point;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GameMapMapper {
    public MapClientState toGameMapState(GameMap gameMap) {
        MapClientState mapClientState = new MapClientState();
        mapClientState.setWidth(gameMap.getWidth());
        mapClientState.setHeight(gameMap.getHeight());

        //МБ НАДО ПОМЕНЯТЬ X и Y МЕСТАМИ
        List<TileType> tileTypes = new ArrayList<>();
        for(int i = 0; i < gameMap.getWidth(); i++) {
            for(int j = 0; j < gameMap.getHeight(); j++) {
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
