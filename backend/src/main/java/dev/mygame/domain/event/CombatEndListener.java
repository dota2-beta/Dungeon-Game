package dev.mygame.domain.event;

import dev.mygame.domain.model.Entity;
import dev.mygame.enums.CombatOutcome;
import dev.mygame.domain.session.CombatInstance;

import java.util.List;

public interface CombatEndListener {
    void onCombatEnded(CombatInstance combat, CombatOutcome outcome, String winningTeamId, List<Entity> allParticipants);
}
