package com.abs.dungeonCrawler.eventcontracts.eventDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntityStatsUpdatedEvent {
    private String sessionId;
    private String entityId;
    private int absorbedByArmor;
    private int damageToHp;
    private int currentHp;
    private int currentDefense;
    private boolean isDead;
    private Integer healToHp;
}
