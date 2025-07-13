package dev.mygame.dto.websocket;

import dev.mygame.enums.TileType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TileDto {
    private int q;
    private int r;
    private TileType type;
}
