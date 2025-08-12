package dev.mygame.service.internal;

import dev.mygame.domain.model.map.Hex;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SpawnPointInfo {
    /**
     * Гексагональная координата точки спавна.
     */
    private Hex position;

    /**
     * Символ, отвечающий за тип монстра.
     */
    private char symbol;

}
