package dev.mygame.service;

import dev.mygame.data.templates.AbilityTemplate;
import dev.mygame.data.templates.EffectTemplate;
import dev.mygame.domain.effects.AbilityContext;
import dev.mygame.domain.effects.AbilityEffect;
import dev.mygame.domain.model.Entity;
import dev.mygame.domain.model.Player;
import dev.mygame.domain.model.map.Hex;
import dev.mygame.domain.session.AbilityInstance;
import dev.mygame.domain.session.GameSession;
import dev.mygame.dto.websocket.event.AbilityCastedEvent;
import dev.mygame.enums.AbilityUseResult;
import dev.mygame.enums.EntityStateType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AbilityService {
    private final Map<String, AbilityEffect> strategies;
    private Logger log = LoggerFactory.getLogger(AbilityService.class);

    public AbilityService(@Autowired List<AbilityEffect> strategies) {
        this.strategies = strategies.stream()
                .collect(Collectors.toMap(AbilityEffect::getEffectType, Function.identity()));
    }

    public AbilityUseResult useAbility(GameSession gameSession, Entity caster, AbilityInstance abilityInstance, Hex targetHex) {
        boolean isInCombat = caster.getState() == EntityStateType.COMBAT;
        if(!abilityInstance.isReady(caster.getState())) {
            if(caster instanceof Player)
                gameSession.sendErrorMessageToPlayer((Player) caster, "On cooldown", "400");
            return AbilityUseResult.ON_COOLDOWN;
        }

        AbilityTemplate abilityTemplate = abilityInstance.getTemplate();

        if(abilityTemplate.getCostAp() > caster.getCurrentAP()
           && isInCombat) {
            if(caster instanceof Player)
                gameSession.sendErrorMessageToPlayer((Player) caster, "Not enough AP", "400");
            return AbilityUseResult.NOT_ENOUGH_AP;
        }

        if(targetHex.distanceTo(caster.getPosition()) > abilityTemplate.getRange()) {
            if(caster instanceof Player)
                gameSession.sendErrorMessageToPlayer((Player) caster, "Out of range", "400");
            return AbilityUseResult.OUT_OF_RANGE;
        }

        if (isInCombat) {
            caster.setCurrentAP(caster.getCurrentAP() - abilityTemplate.getCostAp());
            // в бою устанавливаем КД в ходах
            abilityInstance.setTurnCooldown(abilityTemplate.getCooldown());
        } else {
            // вне боя устанавливаем КД по времени
            long secondsToWait = abilityTemplate.getCooldown() * 10L;
            abilityInstance.setCooldownEndTime(System.currentTimeMillis() + secondsToWait * 1000);
        }

        gameSession.publishCasterStateUpdate(caster);

        AbilityCastedEvent castedEvent = AbilityCastedEvent.builder()
                .casterId(caster.getId())
                .abilityTemplateId(abilityTemplate.getTemplateId())
                .targetHex(targetHex)
                .build();
        gameSession.publishUpdate("ability_casted", castedEvent);

        List<Entity> affectedTargets = gameSession.findTargetsInArea(targetHex, abilityTemplate.getAreaOfEffectRadius());

        if (affectedTargets.isEmpty()) {
            return AbilityUseResult.SUCCESS;
        }

        for (EffectTemplate effectData : abilityTemplate.getEffects()) {
            AbilityEffect strategy = strategies.get(effectData.getType());
            if(strategy != null) {
                AbilityContext abilityContext = AbilityContext.builder()
                        .effectData(effectData)
                        .caster(caster)
                        .gameSession(gameSession)
                        .targetHex(targetHex)
                        .targets(affectedTargets)
                        .build();
                strategy.apply(abilityContext);
            } else {
                log.warn("No ability effect strategy found for type: {}", effectData.getType());
            }
        }
        return AbilityUseResult.SUCCESS;
    }
}
