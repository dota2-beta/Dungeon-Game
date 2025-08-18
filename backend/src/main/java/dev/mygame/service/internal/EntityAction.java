package dev.mygame.service.internal;

import dev.mygame.domain.model.map.Hex;
import dev.mygame.enums.ActionType;
import lombok.*;

/**
 * DTO, описывающий одно действие сущности,
 * такое как движение, атака или использование способности.
 */
@Getter
@Setter
@Builder
@ToString
public class EntityAction {
    private ActionType actionType;
    private String targetId;
    private Hex targetHex;
    private String itemId;
    private String abilityId;
}
