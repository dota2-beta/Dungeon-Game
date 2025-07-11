package dev.mygame.dto.websocket.request;

import dev.mygame.enums.ActionType;
import dev.mygame.domain.model.map.Point;
import lombok.Data;

@Data
public class PlayerAction {
    private ActionType actionType;
    private String targetId;
    private Point targetPoint;
    private String itemId;
}
