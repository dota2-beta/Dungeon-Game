package dev.mygame.dto;

import dev.mygame.game.enums.ActionType;
import dev.mygame.game.model.map.Point;
import lombok.Data;

@Data
public class PlayerAction { // DTO от клиента
    private ActionType actionType;
    private String targetId;
    private Point targetPoint;
    private String itemId;
}
