package dev.mygame.service;

import dev.mygame.config.MapGenerationProperties;
import dev.mygame.domain.model.map.*;
import dev.mygame.enums.TileType;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@NoArgsConstructor
public class MapGenerator {
    private final Random random = new Random();

    public GameMapHex generateHexBattleArena(MapGenerationProperties props) {
        GameMapHex map = new GameMapHex();
        int radius = props.getBattleArenaRadius();
        System.out.println("--- GENERATING ARENA WITH RADIUS: " + radius + " ---");

        for (int q = -radius; q <= radius; q++) {
            for (int r = -radius; r <= radius; r++) {
                Hex hex = new Hex(q, r);
                if (hex.distanceTo(new Hex(0, 0)) <= radius) {
                    map.setTile(hex, new Tile(TileType.FLOOR, null));
                }
            }
        }
        return map;
    }

    public GameMapHex generateHexDungeon(int mapRadius, int numCaverns, int cavernSize) {
        GameMapHex gameMapHex = new GameMapHex();
        List<Hex> roomCenters = new ArrayList<>();

        for(int i = 0; i < numCaverns; ++i) {
            Hex randomCenter = getRandomHexInRadius(mapRadius);
            createCavern(gameMapHex, randomCenter, cavernSize);
            roomCenters.add(randomCenter);
        }

        for(int i = 0; i < roomCenters.size() - 1; ++i) {
            drawHexLine(gameMapHex, roomCenters.get(i), roomCenters.get(i+1));
        }
        return gameMapHex;
    }

    private Hex getRandomHexInRadius(int radius) {
        while (true) {
            int q = -radius + random.nextInt(2 * radius + 1);
            int r = -radius + random.nextInt(2 * radius + 1);
            Hex randomHex = new Hex(r, q);
            if (randomHex.distanceTo(new Hex(0, 0)) <= radius)
                return randomHex;
        }
    }

    private void drawHexLine(GameMapHex gameMapHex, Hex hex1, Hex hex2) {

    }

    private void createCavern(GameMapHex gameMapHex, Hex startHex, int numSteps) {
        Hex currentHex = startHex;
        for(int i = 0; i < numSteps; ++i) {
            gameMapHex.setTile(currentHex, new Tile(TileType.FLOOR, null));
            int neighbour = random.nextInt(Hex.DIRECTIONS.size());
            Hex randomDirection = Hex.DIRECTIONS.get(neighbour);
            currentHex = currentHex.add(randomDirection);
        }
    }
}
