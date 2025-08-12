package dev.mygame.dto.websocket.request;

import lombok.Data;

/**
 * DTO для подключения новых пользователей к существующей сессии
 */
@Data
public class JoinRequest {
    public String sessionId;
    public String userId;
}
