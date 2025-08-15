package dev.mygame.dto.websocket.event;

import dev.mygame.dto.websocket.response.CombatTeamDto;
import dev.mygame.dto.websocket.response.EntityStateDto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CombatStartedEvent {
    private String combatId;
    private String combatInitiatorId;
    private List<CombatTeamDto> teams;
    private List<String> initialTurnOrder;
    private List<EntityStateDto> combatants;
}
