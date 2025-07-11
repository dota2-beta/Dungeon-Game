package dev.mygame.dto.websocket.response.event;

import dev.mygame.domain.model.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CombatStartedEvent {
    private String combatId;
    private String combatInitiatorId;
    private Map<String, Entity> team1EntityIds; // переделать на Set
    private Map<String, Entity> team2EntityIds; // переделать на Set
}
