package dev.mygame.dto.websocket.response.event;

import dev.mygame.dto.websocket.response.AbilityCooldownDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CasterStateUpdatedEvent {
    private String casterId;
    private int newCurrentAP;
    private List<AbilityCooldownDto> abilityCooldowns;
}
