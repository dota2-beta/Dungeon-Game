package dev.mygame.domain.event;

import dev.mygame.domain.model.Entity;
import dev.mygame.domain.model.Monster;
import dev.mygame.enums.CombatOutcome;

import java.util.List;

public interface CombatEventListener {
    Entity getEntityById(String id);
    void scheduleAiTurn(String monsterId);

    void onCombatEvent(Object event);
    void onCombatEnded(String combatId, CombatOutcome outcome, String winningTeamId, List<Entity> allParticipants);
}
