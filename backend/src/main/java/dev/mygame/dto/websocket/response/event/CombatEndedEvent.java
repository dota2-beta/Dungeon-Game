package dev.mygame.dto.websocket.response.event;

import dev.mygame.enums.CombatOutcome;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CombatEndedEvent {
    private String combatId;
    private CombatOutcome outcome;
    private String winningTeamId;
}
