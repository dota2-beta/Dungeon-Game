package dev.mygame.dto.websocket.request;

import dev.mygame.domain.model.map.Hex;
import dev.mygame.enums.ActionType;
import lombok.Data;

@Data
public class PlayerAction {
    private ActionType actionType;
    private String targetId;
    private Hex targetHex;
    private String itemId;
}
