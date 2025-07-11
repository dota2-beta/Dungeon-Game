package dev.mygame.dto.websocket.response;

import lombok.Data;

import java.util.List;

@Data
public class GameSessionState {
    public String sessionId;
    public String yourPlayerId; // id сущности, которой управляет клиент
    public MapState mapState;
    public List<EntityState> entities;
}
