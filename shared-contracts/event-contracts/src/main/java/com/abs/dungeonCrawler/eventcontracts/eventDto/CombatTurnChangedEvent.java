package com.abs.dungeonCrawler.eventcontracts.eventDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CombatTurnChangedEvent {
    private String sessionId;
    private String combatId;
    private String activeEntityId;
    private int currentAP;
    //private List<AbilityCooldownDto> abilityCooldowns;
}
