package dev.mygame.dto.websocket.request;

import dev.mygame.domain.model.map.Hex;
import dev.mygame.enums.ActionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerAction {
    private ActionType actionType;
    private String targetId;
    private Hex targetHex;
    private String itemId;
    private String abilityId;
}
