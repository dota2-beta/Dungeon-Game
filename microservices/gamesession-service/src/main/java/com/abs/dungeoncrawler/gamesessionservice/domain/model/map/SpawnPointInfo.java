package com.abs.dungeoncrawler.gamesessionservice.domain.model.map;

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
    //private Character symbol;
    private String templateId;
}
