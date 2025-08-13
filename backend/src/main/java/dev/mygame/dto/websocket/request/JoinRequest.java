package dev.mygame.dto.websocket.request;

import lombok.Data;

/**
 * DTO для подключения новых пользователей к существующей сессии
 */
@Data
public class JoinRequest {
    private String sessionId;
    private String name;
    private String templateId;
}
