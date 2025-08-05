package dev.mygame.dto.websocket.request;

import lombok.Data;

@Data
public class InviteToTeamRequest {
    private String targetPlayerId; // кого приглашаем
}
