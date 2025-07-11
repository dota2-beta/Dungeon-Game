package dev.mygame.data;

import lombok.Data;

@Data
public class DungeonTemplate {
    private String name;
    private int minMonsters;
    private int maxMonsters;
    private int minMonstersLvl;
    private int maxMonstersLvl;
    private int minLootLvlChest;
    private int maxLootLvlChest;
}
