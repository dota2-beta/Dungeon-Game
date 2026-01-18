package com.abs.dungeoncrawler.gamesessionservice.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DamageResult implements EffectResult {
    private String entityId;
    private int damage;
    private int absorbedByArmor;
    private int damageToHp;
    private boolean isDead;
}
