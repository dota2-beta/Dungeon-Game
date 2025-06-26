package dev.mygame.dto;

import dev.mygame.game.model.map.Point;
import lombok.Data;

@Data
public class EntityClientState {
    public String id;
    public String name;
    public Point position;
    public int currentHp;
    public int maxHp;
    public String state;
    public String type; // игрок или монстр
}
