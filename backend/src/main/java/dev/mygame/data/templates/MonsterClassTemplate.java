package dev.mygame.data.templates;

import lombok.Data;

@Data
public class MonsterClassTemplate {
    private String templateId;
    private String name;
    private EntityStatsTemplate stats;
    // private List<String> abilities; // для будущего
}