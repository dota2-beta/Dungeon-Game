package dev.mygame.game.session;

import dev.mygame.game.enums.TileType;
import dev.mygame.game.model.map.GameMap;
import dev.mygame.game.model.map.Tile;
import org.springframework.stereotype.Component;

@Component
public class MapGenerator {

    public GameMap generateMap() {
        int width = 10;
        int height = 10;

        GameMap gameMap = new GameMap(width,height);

        Tile[][] tiles = new Tile[width][height];
        for(int i = 0; i < 10; ++ i)
            for(int j = 0; j < 10; ++ j)
            {
                //TileType type = (i == 0 || i == width - 1 || j == 0 || j == height - 1) ? TileType.WALL : TileType.FLOOR;
                TileType type = TileType.FLOOR;
                tiles[i][j] = new Tile(type, null);
            }
        gameMap.setTiles(tiles);
        return gameMap;
    }
}
