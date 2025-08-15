package dev.mygame.dto.websocket.event;

import dev.mygame.dto.websocket.response.PlayerStateDto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlayerJoinedEvent {
    private PlayerStateDto player;
}
