package dev.mygame.dto.websocket.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntityAttackEvent {
    private String attackerEntityId;
    private String targetEntityId;
    private int damageCaused;
    private int attackerCurrentAP;
}
