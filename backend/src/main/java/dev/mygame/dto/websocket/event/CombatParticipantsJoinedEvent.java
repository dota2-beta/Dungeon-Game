package dev.mygame.dto.websocket.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CombatParticipantsJoinedEvent {
    private String combatId;
    private Set<String> participantIds;
    private List<String> turnOrder;
}
