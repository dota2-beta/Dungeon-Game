package dev.mygame.mapper;

import dev.mygame.domain.model.map.*;
import dev.mygame.dto.websocket.TileDto;
import dev.mygame.dto.websocket.response.MapStateDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GameMapMapper {
    public MapStateDto toGameMapState(GameMapHex gameMap) {
        MapStateDto mapClientState = new MapStateDto();
        List<TileDto> tiles = gameMap.getTileEntries().stream()
                        .map(entry -> new TileDto(
                                entry.getKey().getQ(),
                                entry.getKey().getR(),
                                entry.getValue().getType()))
                        .toList();
        mapClientState.setTiles(tiles);

        List<Hex> spawnPoints = new ArrayList<>();
        spawnPoints.add(new Hex(0, 0));

        mapClientState.setSpawnPoints(spawnPoints);
        return mapClientState;
    }
}
