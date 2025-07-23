package dev.mygame.dto.websocket.response.event;

import dev.mygame.domain.model.map.Hex;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntityMovedEvent {
    private String entityId;
    private Hex newPosition;
    private int currentAp;
    private List<Hex> pathToAnimate;
    private boolean reachedTarget;
}
