package dev.mygame.dto.websocket.request;

import lombok.Data;

/**
 * DTO для отправки соглашения (или отказа) на приглашение присоединиться к команде
 */
@Data
public class RespondToTeamRequest {
    private boolean accepted;
}
