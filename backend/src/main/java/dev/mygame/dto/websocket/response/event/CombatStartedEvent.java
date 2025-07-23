package dev.mygame.dto.websocket.response.event;

import dev.mygame.domain.model.Entity;
import dev.mygame.dto.websocket.response.EntityStateDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Builder
public class CombatStartedEvent {
    private String combatId;
    private String combatInitiatorId;
    private List<CombatTeamDto> teams;
    private List<String> initialTurnOrder;
    private List<EntityStateDto> combatants;
}
