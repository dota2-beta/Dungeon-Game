package dev.mygame.game.session;

import dev.mygame.game.enums.ActionType;
import dev.mygame.game.model.map.Point;
import dev.mygame.game.session.event.IAction;
import lombok.Data;

@Data
public class EntityAction implements IAction { // Внутренний командный объект
    private ActionType actionType;
    private String targetId;
    private Point targetPoint;
    private String itemId;
}
