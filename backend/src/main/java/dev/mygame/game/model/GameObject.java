package dev.mygame.game.model;

import dev.mygame.game.model.map.Point;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public abstract class GameObject extends GameMapObject{
    private String type;
}
