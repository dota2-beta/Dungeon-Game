package com.abs.dungeonCrawler.eventcontracts.eventDto;

import com.abs.dungeonCrawler.eventcontracts.Dto.EntityStateDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CombatStartedEvent {
    private String sessionId;
    private String combatId;
    private String combatInitiatorId;
    private List<CombatTeamDto> teams;
    private List<String> initialTurnOrder;
    private List<EntityStateDto> combatants;
}
