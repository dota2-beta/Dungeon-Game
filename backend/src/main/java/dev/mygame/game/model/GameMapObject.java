package dev.mygame.game.model;

import dev.mygame.game.model.map.Point;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public abstract class GameMapObject {
    protected String id;
    protected Point position;
}
