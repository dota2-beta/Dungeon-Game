package dev.mygame.dto.websocket.response.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TeamInviteEvent {
    private String fromPlayerId;
    private String fromPlayerName;
    private String toTeamId;
}
