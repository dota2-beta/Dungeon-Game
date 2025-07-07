package dev.mygame.dto;

import dev.mygame.game.model.Entity;
import dev.mygame.game.model.GameObject;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GameSessionState {
    public String sessionId;
    public String yourPlayerId; // id сущности, которой управляет клиент
    public MapClientState mapState;
    public List<EntityClientState> entities;
}
