package dev.mygame.dto.websocket.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbilityCooldownDto {
    private String abilityTemplateId;
    private int turnCooldown;
    private long cooldownEndTime;
}
