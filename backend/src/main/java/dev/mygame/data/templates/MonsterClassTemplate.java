package dev.mygame.data.templates;

import lombok.Data;

import java.util.List;

@Data
public class MonsterClassTemplate {
    private String templateId;
    private String name;
    private List<String> abilities;
    private EntityStatsTemplate stats;
}