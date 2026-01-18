package com.abs.dungeonCrawler.eventcontracts.eventDto;

import com.abs.dungeonCrawler.eventcontracts.Dto.EntityStateDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CombatParticipantsJoinedEvent {
    private String sessionId;
    private String combatId;
    private List<EntityStateDto> participants;
    private List<String> turnOrder;
    private String activeEntityId;
}