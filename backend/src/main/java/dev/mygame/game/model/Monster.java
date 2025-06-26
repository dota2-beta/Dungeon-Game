package dev.mygame.game.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
//@AllArgsConstructor
//@NoArgsConstructor
public class Monster extends Entity {
    private String type;
    private int experienceValue;
}
