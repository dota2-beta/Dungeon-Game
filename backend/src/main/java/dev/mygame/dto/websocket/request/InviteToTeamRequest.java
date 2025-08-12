package dev.mygame.dto.websocket.request;

import lombok.Data;

/**
 * DTO для получения сервером приглашения от одного пользователь к другому
 */
@Data
public class InviteToTeamRequest {
    private String targetPlayerId; // кого приглашаем
}
