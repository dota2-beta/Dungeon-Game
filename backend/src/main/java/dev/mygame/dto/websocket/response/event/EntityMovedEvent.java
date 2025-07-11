package dev.mygame.dto.websocket.response.event;

import dev.mygame.domain.model.map.Point;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntityMovedEvent {
    private String entityId;
    private Point newPosition;
    private int remainingAp;
    private List<Point> pathToAnimate;
    private boolean reachedTarget;
}
