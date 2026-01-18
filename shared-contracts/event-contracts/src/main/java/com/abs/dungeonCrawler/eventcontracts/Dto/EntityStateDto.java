package com.abs.dungeonCrawler.eventcontracts.Dto;

import com.abs.dungeonCrawler.eventcontracts.enums.EntityState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EntityStateDto {
    public String id;
    public String name;
    public HexDto position;
    public int currentHp;
    public int maxHp;
    public EntityState state;
    public int currentAP;
    public int defense;
    public int maxAP;
    public String type; // игрок или монстр
    public String teamId;
    //private List<AbilityCooldownDto> abilities;
}