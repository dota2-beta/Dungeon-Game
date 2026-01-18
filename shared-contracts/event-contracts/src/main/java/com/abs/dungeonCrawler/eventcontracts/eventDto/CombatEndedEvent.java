package com.abs.dungeonCrawler.eventcontracts.eventDto;

import com.abs.dungeonCrawler.eventcontracts.enums.CombatOutcome;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CombatEndedEvent {
    private String sessionId;
    private String combatId;
    private CombatOutcome outcome;
    private String winningTeamId;
}
