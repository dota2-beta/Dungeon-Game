package dev.mygame.domain.effects;

import dev.mygame.domain.model.Entity;
import dev.mygame.domain.session.GameSession;
import dev.mygame.dto.websocket.event.EntityStatsUpdatedEvent;
import dev.mygame.service.internal.DamageResult;
import dev.mygame.service.internal.EffectResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DamageEffect implements AbilityEffect {
    @Override
    public List<EffectResult> apply(AbilityContext context) {
        int damageAmount = context.getEffectData().getAmount();
        List<Entity> targetEntities = context.getTargets();
        List<EffectResult> results = new ArrayList<>();

        for(Entity entity : targetEntities) {
            if(entity.isDead())
                continue;
            DamageResult damageResult = entity.takeDamage(damageAmount);
            results.add(damageResult);
        }
        return results;
    }

    @Override
    public String getEffectType() {
        return "DAMAGE";
    }
}
