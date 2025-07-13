package dev.mygame.dto.websocket.response;

import lombok.Data;

import java.util.List;

@Data
public class GameSessionStateDto {
    public String sessionId;
    public String yourPlayerId; // id сущности, которой управляет клиент
    public MapStateDto mapState;
    public List<EntityStateDto> entities;
}
