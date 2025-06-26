package dev.mygame.dto;

import dev.mygame.game.model.map.Point;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PlayerClientState extends EntityClientState {
    public String userId;

}
