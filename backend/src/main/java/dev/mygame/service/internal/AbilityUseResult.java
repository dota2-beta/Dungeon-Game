package dev.mygame.service.internal;

import dev.mygame.domain.model.Entity;
import dev.mygame.domain.model.map.Hex;
import dev.mygame.enums.AbilityUseResultEnum;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class AbilityUseResult {
    private final AbilityUseResultEnum success;
    private final Entity caster;
    private final String abilityTemplateId;
    private final Hex targetHex;
    private final List<Entity> affectedTargets;
    @Builder.Default
    private final List<EffectResult> effectsResults = new ArrayList<>();
}