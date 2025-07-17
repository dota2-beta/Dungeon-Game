package dev.mygame.domain.event;

import dev.mygame.enums.CombatOutcome;
import dev.mygame.domain.session.CombatInstance;

public interface CombatEndListener {
    void onCombatEnded(CombatInstance combat, CombatOutcome outcome, String forTeamId);
}
