package com.abs.dungeoncrawler.gamesessionservice.domain.model;

import com.abs.dungeoncrawler.gamesessionservice.domain.model.map.Hex;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public abstract class GameMapObject {
    protected String id;
    protected Hex position;
}
