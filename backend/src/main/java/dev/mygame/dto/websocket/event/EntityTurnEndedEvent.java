package dev.mygame.dto.websocket.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntityTurnEndedEvent {
    private String entityId;
}
