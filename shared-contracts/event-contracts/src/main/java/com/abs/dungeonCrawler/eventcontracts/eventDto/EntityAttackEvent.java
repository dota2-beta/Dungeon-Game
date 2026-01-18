package com.abs.dungeonCrawler.eventcontracts.eventDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntityAttackEvent {
    private String sessionId;
    private String attackerEntityId;
    private String targetEntityId;
    private int damageCaused;
    private int attackerCurrentAP;
}