package dev.mygame.dto.websocket.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EntityTurnEndEvent {
    private String currentTurnEntityId;
}
