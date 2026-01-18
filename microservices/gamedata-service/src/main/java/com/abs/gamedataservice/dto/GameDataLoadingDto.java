package com.abs.gamedataservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class GameDataLoadingDto {
    @JsonProperty("entity-classes")
    List<EntityTemplateDto> entityTemplateDtoList;

    @Data
    public static class EntityTemplateDto {
        private String templateId;
        private String name;
        private String description;
        private String type;
        private List<String> abilities;
        private StatsDto stats;
    }

    @Data
    public static class StatsDto {
        private int maxHp;
        private int attack;
        private int defense;
        private int initiative;
        private int maxAP;
        private int attackRange;
        private int aggroRadius;
    }
}
