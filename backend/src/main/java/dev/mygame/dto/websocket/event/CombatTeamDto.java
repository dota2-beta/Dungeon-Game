package dev.mygame.dto.websocket.event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class CombatTeamDto {
    private String teamId;
    private Set<String> memberIds;
}
