package dev.mygame.dto.websocket.response;

import dev.mygame.domain.model.map.Point;
import dev.mygame.enums.EntityStateType;
import lombok.Data;

@Data
public class EntityState {
    public String id;
    public String name;
    public Point position;
    public int currentHp;
    public int maxHp;
    public EntityStateType state;
    public String type; // игрок или монстр
    public boolean isDead;
}
