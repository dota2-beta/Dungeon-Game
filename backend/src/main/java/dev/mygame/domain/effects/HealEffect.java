package dev.mygame.domain.effects;

import dev.mygame.domain.model.Entity;
import dev.mygame.domain.session.GameSession;
import dev.mygame.dto.websocket.event.EntityStatsUpdatedEvent;
import dev.mygame.service.internal.EffectResult;
import dev.mygame.service.internal.HealResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class HealEffect implements AbilityEffect {
    @Override
    public List<EffectResult> apply(AbilityContext context) {
        int heal = context.getEffectData().getAmount();
        List<Entity> targetEntities = context.getTargets();
        List<EffectResult> results = new ArrayList<>();

        for(Entity entity : targetEntities){
            if(entity.isDead())
                continue;

            HealResult healResult = entity.takeHeal(heal);

            if (healResult.getActualHealedAmount() <= 0) {
                continue;
            }
            results.add(healResult);
        }
        return results;
    }

    @Override
    public String getEffectType() {
        return "HEAL";
    }
}
