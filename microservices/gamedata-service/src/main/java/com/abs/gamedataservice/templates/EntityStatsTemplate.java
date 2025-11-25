package com.abs.gamedataservice.templates;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
@Builder
public class EntityStatsTemplate {
    private int maxHp;
    private int attack;
    private int defense;
    private int initiative;
    private int maxAP;
    private int attackRange;
    private int aggroRadius;
}
