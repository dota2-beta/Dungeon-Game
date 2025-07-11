package dev.mygame.service;

import dev.mygame.enums.TileType;
import dev.mygame.domain.model.map.GameMap;
import dev.mygame.domain.model.map.Tile;
import org.springframework.stereotype.Component;

@Component
public class MapGenerator {

    public GameMap generateMap() {
        int width = 25;
        int height = 25;

        GameMap gameMap = new GameMap(width,height);

        Tile[][] tiles = new Tile[width][height];
        for(int i = 0; i < width; ++ i)
            for(int j = 0; j < height; ++ j)
            {
                //TileType type = (i == 0 || i == width - 1 || j == 0 || j == height - 1) ? TileType.WALL : TileType.FLOOR;
                TileType type = TileType.FLOOR;
                tiles[i][j] = new Tile(type, null);
            }
        gameMap.setTiles(tiles);
        return gameMap;
    }
}
