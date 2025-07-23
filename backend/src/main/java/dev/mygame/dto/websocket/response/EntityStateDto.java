package dev.mygame.dto.websocket.response;

import dev.mygame.domain.model.map.Hex;
import dev.mygame.enums.EntityStateType;
import lombok.Data;

@Data
public class EntityStateDto {
    public String id;
    public String name;
    public Hex position;
    public int currentHp;
    public int maxHp;
    public EntityStateType state;
    public int currentAP;
    public int maxAP;
    public String type; // игрок или монстр
    public boolean isDead;
}
