package dev.mygame.domain.effects;

import dev.mygame.data.templates.AbilityTemplate;
import dev.mygame.data.templates.EffectTemplate;
import dev.mygame.domain.model.Entity;
import dev.mygame.domain.model.map.Hex;
import dev.mygame.domain.session.GameSession;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AbilityContext {
    private Entity caster;
    private List<Entity> targets;
    private Hex targetHex;
    private GameSession gameSession;
    private EffectTemplate effectData;
}
