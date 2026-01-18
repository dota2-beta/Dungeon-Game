package com.abs.dungeoncrawler.gamesessionservice.domain.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class Player extends Entity {
    //private List<Item> inventory;
    private String userId;
}
