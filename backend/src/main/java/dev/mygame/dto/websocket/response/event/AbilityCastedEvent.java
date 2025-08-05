package dev.mygame.dto.websocket.response.event;

import dev.mygame.domain.model.map.Hex;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbilityCastedEvent {
    private String casterId;
    private String abilityTemplateId;
    private Hex targetHex;
    // private List<String> targetIds;
}
