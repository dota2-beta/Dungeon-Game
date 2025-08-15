package dev.mygame.dto.websocket.response;

import dev.mygame.domain.model.map.Hex;
import dev.mygame.dto.websocket.TileDto;
import lombok.Data;

import java.util.List;

@Data
public class MapStateDto {
    List<TileDto> tiles;
    List<Hex> spawnPoints;
}
