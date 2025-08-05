package dev.mygame.data;

import dev.mygame.domain.model.map.GameMapHex;
import dev.mygame.domain.model.map.Hex;
import dev.mygame.domain.model.map.Tile;
import dev.mygame.enums.TileType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MapLoader {
    public GameMapHex loadMapFromFile(String filePath) throws Exception {
        List<String> lines = readLinesFromResource(filePath);
        Map<Hex, Tile> tiles = new HashMap<>();
        List<Hex> playerSpawnPoints = new ArrayList<>();
        List<Hex> monsterSpawnPoints = new ArrayList<>();

        for(int row = 0; row < lines.size(); row++) {
            String line = lines.get(row);
            if(line.trim().isEmpty() || line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            line = line.trim();
            for(int col = 0; col < line.length(); col++) {
                char symbol = line.charAt(col);
                if(symbol == '-')
                    continue;

                int offset = row / 2; // Целочисленное деление
                int q = col - offset;
                int r = row;
                Hex hex = new Hex(q, r);

                TileType type = TileType.FLOOR;
                boolean isSpawnPoint = false;

                switch (symbol) {
                    case 'W':
                        type = TileType.WALL;
                        break;
                    case 'D':
                        type = TileType.DOOR;
                        break;
                    case '.':
                        type = TileType.FLOOR;
                        break;
                    case 'P':
                        playerSpawnPoints.add(hex);
                        isSpawnPoint = true;
                        break;
                    case 'M':
                        monsterSpawnPoints.add(hex);
                        isSpawnPoint = true;
                        break;
                    case 'B':
                        // отдельный список для боссов
                        monsterSpawnPoints.add(hex);
                        isSpawnPoint = true;
                        break;
                }
                Tile tile = new Tile(isSpawnPoint ? TileType.FLOOR : type, null);
                tiles.put(hex, tile);
            }
        }
        //return new GameMapHex(tiles, playerSpawnPoints, monsterSpawnPoints);
        return GameMapHex.builder()
                .tiles(tiles)
                .playerSpawnPoints(playerSpawnPoints)
                .monsterSpawnPoints(monsterSpawnPoints)
                .build();
    }

    private List<String> readLinesFromResource(String filePath) throws Exception {
        ClassPathResource resource = new ClassPathResource(filePath);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().toList();
        }
    }
}
