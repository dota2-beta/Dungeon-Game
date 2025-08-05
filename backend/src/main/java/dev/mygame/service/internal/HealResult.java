package dev.mygame.service.internal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HealResult {
    private int actualHealedAmount;
    private int newCurrentHp;
}
