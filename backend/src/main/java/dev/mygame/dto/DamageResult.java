package dev.mygame.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DamageResult {
    private int damage;
    private int absorbedByArmor;
    private int damageToHp;
}
