package dev.mygame.service.internal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HealResult implements EffectResult {
    private String entityId;
    private int actualHealedAmount;
    private int newCurrentHp;
}
