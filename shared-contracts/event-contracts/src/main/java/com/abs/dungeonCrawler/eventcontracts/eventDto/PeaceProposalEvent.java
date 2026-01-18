package com.abs.dungeonCrawler.eventcontracts.eventDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeaceProposalEvent {
    private String sessionId;
    private String combatId;
    private String initiatorName;
    private String initiatorId;
}
