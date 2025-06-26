package dev.mygame.game.session;

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
                tiles[i][j] = new Tile();
            }
        gameMap.setTiles(tiles);
        return gameMap;
    }
}
