package dev.mygame.domain.model;

import dev.mygame.domain.model.map.Point;
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
