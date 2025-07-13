package dev.mygame.dto.websocket.response.event;

import dev.mygame.domain.model.map.Hex;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntityMovedEvent {
    private String entityId;
    private Hex newPosition;
    private int remainingAp;
    private List<Hex> pathToAnimate;
    private boolean reachedTarget;
}
