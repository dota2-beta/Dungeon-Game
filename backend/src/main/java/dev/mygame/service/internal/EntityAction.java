package dev.mygame.service.internal;

import dev.mygame.enums.ActionType;
import dev.mygame.domain.model.map.Point;
import dev.mygame.domain.event.IAction;
import lombok.Data;

@Data
public class EntityAction implements IAction {
    private ActionType actionType;
    private String targetId;
    private Point targetPoint;
    private String itemId;
}
