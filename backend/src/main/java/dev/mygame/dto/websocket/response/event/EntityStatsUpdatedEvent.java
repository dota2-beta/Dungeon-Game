package dev.mygame.dto.websocket.response.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntityStatsUpdatedEvent {
    private String targetEntityId;
    private int absorbedByArmor;
    private int damageToHp;
    private int currentHp;
    private int currentDefense;
    private boolean isDead;
    private Integer healToHp;
}
