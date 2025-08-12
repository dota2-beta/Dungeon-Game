package dev.mygame.data.templates;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Data
public class EntityStatsTemplate {
    @Min(1)
    private int maxHp;

    @Min(0)
    private int attack;

    @Min(0)
    private int defense;

    @NotBlank
    private int initiative;

    @Min(2)
    private int maxAP;

    @Min(0)
    private int attackRange;

    @Min(0)
    private int aggroRadius;
}
