package dev.mygame.dto;

import dev.mygame.game.enums.EntityState;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class MonsterClientState extends EntityClientState {
    public String monsterType;
}
