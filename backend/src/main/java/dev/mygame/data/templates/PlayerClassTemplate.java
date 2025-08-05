package dev.mygame.data.templates;

import lombok.Data;

import java.util.List;

@Data
public class PlayerClassTemplate {
    private String templateId;
    private String name;
    private String description;
    private List<String> abilities;
    private EntityStatsTemplate stats;
}