package com.abs.dungeoncrawler.gamesessionservice.domain.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class Monster extends Entity {
    private String templateId;
    //private Character type;
}
