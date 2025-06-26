package dev.mygame.game.session.event;

import dev.mygame.game.enums.CombatOutcome;
import dev.mygame.game.session.CombatInstance;

public interface CombatEndListener {
    void onCombatEnded(CombatInstance combatInstance, CombatOutcome outcome);
}
