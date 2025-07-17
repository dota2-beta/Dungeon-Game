package dev.mygame.dto.websocket.response.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CombatNextTurnEvent {
    private String combatId;
    private String currentTurnEntityId;
    private int currentAp;
}
