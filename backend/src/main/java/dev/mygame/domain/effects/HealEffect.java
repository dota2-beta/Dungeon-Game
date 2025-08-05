package dev.mygame.domain.effects;

import dev.mygame.domain.model.Entity;
import dev.mygame.domain.session.GameSession;
import dev.mygame.dto.websocket.response.event.EntityStatsUpdatedEvent;
import dev.mygame.service.internal.HealResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HealEffect implements AbilityEffect{

    @Override
    public void apply(AbilityContext context) {
        int heal = context.getEffectData().getAmount();
        List<Entity> targetEntities = context.getTargets();
        GameSession gameSession = context.getGameSession();

        for(Entity entity : targetEntities){
            if(entity.isDead())
                continue;

            HealResult healResult = entity.takeHeal(heal);

            if (healResult.getActualHealedAmount() <= 0) {
                continue;
            }

            EntityStatsUpdatedEvent event = EntityStatsUpdatedEvent.builder()
                    .targetEntityId(entity.getId())
                    .currentHp(healResult.getNewCurrentHp())
                    .healToHp(healResult.getActualHealedAmount())
                    .isDead(false)
                    .build();

            gameSession.publishUpdate("entity_stats_updated", event);
        }
    }

    @Override
    public String getEffectType() {
        return "HEAL";
    }
}
