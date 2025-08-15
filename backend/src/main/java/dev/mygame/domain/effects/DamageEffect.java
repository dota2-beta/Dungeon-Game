package dev.mygame.domain.effects;

import dev.mygame.domain.model.Entity;
import dev.mygame.domain.session.GameSession;
import dev.mygame.dto.websocket.event.EntityStatsUpdatedEvent;
import dev.mygame.service.internal.DamageResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DamageEffect implements AbilityEffect{

    @Override
    public void apply(AbilityContext context) {
        int damageAmount = context.getEffectData().getAmount();
        List<Entity> targetEntities = context.getTargets();
        GameSession gameSession = context.getGameSession();

        for(Entity entity : targetEntities) {
            if(entity.isDead())
                continue;
            DamageResult damageResult = entity.takeDamage(damageAmount);
            EntityStatsUpdatedEvent event = EntityStatsUpdatedEvent.builder()
                    .targetEntityId(entity.getId())
                    .damageToHp(damageResult.getDamageToHp())
                    .isDead(damageResult.isDead())
                    .absorbedByArmor(damageResult.getAbsorbedByArmor())
                    .currentDefense(entity.getDefense())
                    .currentHp(entity.getCurrentHp())
                    .build();
            gameSession.publishEvent(event);
        }
    }

    @Override
    public String getEffectType() {
        return "DAMAGE";
    }
}
