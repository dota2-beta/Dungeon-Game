package dev.mygame.dto.websocket.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class MonsterState extends EntityState {
    public String monsterType;
}
