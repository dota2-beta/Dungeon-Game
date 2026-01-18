package com.abs.gamedataservice.data;

import com.dungeoncrawler.contracts.grpc.common.HexMsg;
import com.dungeoncrawler.contracts.grpc.common.TileMsg;
import com.dungeoncrawler.contracts.grpc.gamedata.GameMapResponse;
import com.dungeoncrawler.contracts.grpc.gamedata.SpawnPointInfoMsg;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class MapLoader {
    public GameMapResponse loadMapFromFile(String filePath) throws Exception {
        List<String> lines = readLinesFromResource(filePath);
        List<TileMsg> tiles = new ArrayList<>();
        List<SpawnPointInfoMsg> playerSpawnPoints = new ArrayList<>();
        List<SpawnPointInfoMsg> monsterSpawnPoints = new ArrayList<>();

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

                int offset = row / 2;
                int q = col - offset;
                int r = row;
                HexMsg hex = HexMsg.newBuilder()
                        .setQ(q)
                        .setR(r)
                        .build();

                String type = Character.toString(symbol);

                switch (symbol) {
                    case 'P':
                        playerSpawnPoints.add(SpawnPointInfoMsg.newBuilder()
                                .setCoordinate(hex)
                                .build()
                        );
                        type = ".";
                        break;
                    case 'M':
                        monsterSpawnPoints.add(SpawnPointInfoMsg.newBuilder()
                                .setCoordinate(hex)
                                .setTemplateId(getMonsterTemplateIdBySymbol('M'))
                                .build()
                        );
                        type = ".";
                        break;
                    case 'B':
                        monsterSpawnPoints.add(SpawnPointInfoMsg.newBuilder()
                                .setCoordinate(hex)
                                .setTemplateId(getMonsterTemplateIdBySymbol('B'))
                                .build()
                        );
                        type = ".";
                        break;
                    case 'G':
                        monsterSpawnPoints.add(SpawnPointInfoMsg.newBuilder()
                                .setCoordinate(hex)
                                .setTemplateId(getMonsterTemplateIdBySymbol('G'))
                                .build()
                        );
                        type = ".";
                        break;
                    default:
                        break;
                }
                TileMsg tile = TileMsg.newBuilder()
                        .setCoordinate(hex)
                        .setType(type)
                        .build();
                tiles.add(tile);
            }
        }
        return GameMapResponse.newBuilder()
                .addAllTiles(tiles)
                .addAllPlayerSpawnPoints(playerSpawnPoints)
                .addAllMonsterSpawnPoints(monsterSpawnPoints)
                .build();
    }

    private List<String> readLinesFromResource(String filePath) throws Exception {
        ClassPathResource resource = new ClassPathResource(filePath);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().toList();
        }
    }

    private String getMonsterTemplateIdBySymbol(char symbol) {
        return switch (symbol) {
            case 'M' -> "goblin_warrior";
            case 'B' -> "lich_king";
            case 'G' -> "ghost";
            default -> throw new IllegalArgumentException("Unknown monster symbol: " + symbol);
        };
    }
}