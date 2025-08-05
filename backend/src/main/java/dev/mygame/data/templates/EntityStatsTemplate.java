package dev.mygame.data.templates;

import lombok.Data;

import java.util.List;

@Data
public class EntityStatsTemplate {
    private int maxHp;
    private int attack;
    private int defense;
    private int initiative;
    private int maxAP;
    private int attackRange;
    private int aggroRadius;
}
