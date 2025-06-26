package dev.mygame.game.session;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MonsterTemplate {
    private int id;
    private String name;
    private int baseMaxHp;
    private int baseAttack;
    private int baseDefense;
    private int baseInitiative;
    private int experienceValue;
}
