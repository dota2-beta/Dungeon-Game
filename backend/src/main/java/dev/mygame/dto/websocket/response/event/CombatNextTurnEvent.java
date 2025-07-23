package dev.mygame.dto.websocket.response.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CombatNextTurnEvent {
    private String combatId;
    private String currentTurnEntityId;
    private int currentAp;
}
