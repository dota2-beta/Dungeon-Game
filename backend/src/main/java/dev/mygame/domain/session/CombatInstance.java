package dev.mygame.domain.session;

import dev.mygame.enums.CombatOutcome;
import dev.mygame.enums.EntityStateType;
import dev.mygame.domain.event.CombatEndListener;
import dev.mygame.domain.event.DeathListener;
import dev.mygame.domain.model.Entity;
import dev.mygame.domain.model.Monster;
import dev.mygame.domain.model.Player;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;

@Data
@AllArgsConstructor
public class CombatInstance implements DeathListener {
    private String combatId;
    private GameSession gameSession;

    private Map<String, Entity> team1;
    private Map<String, Entity> team2;

    private Queue<String> turnOrder;
    private String currentTurnEntityId;

    private List<CombatEndListener> endListeners;

    public CombatInstance( String combatId
                         , GameSession gameSession
                         , List<Entity> initParticipants
                         , String currentTurnEntityId ) {
        this.combatId = combatId;
        this.gameSession = gameSession;
        this.team1 = new HashMap<>();
        this.team2 = new HashMap<>();
        this.turnOrder = new LinkedList<>();
        this.currentTurnEntityId = currentTurnEntityId;

        initializeTeamsAndListeners(initParticipants);
        initializeTurnOrder();
    }

    private void initializeTeamsAndListeners(List<Entity> participants) {
        for (Entity entity : participants) {
            if (entity instanceof Player) {
                this.team1.put(entity.getId(), entity);
            } else if (entity instanceof Monster) {
                this.team2.put(entity.getId(), entity);
            }
            entity.setState(EntityStateType.COMBAT);
            entity.addDeathListener(this);
        }
    }

    private void initializeTurnOrder () {
        List<Entity> allParticipants = new ArrayList<>();
        this.team1.values().stream().filter(Entity::isAlive).forEach(allParticipants::add);
        this.team2.values().stream().filter(Entity::isAlive).forEach(allParticipants::add);

        allParticipants.stream()
                .sorted(Comparator.comparing(Entity::getInitiative).reversed())
                .forEach(e -> this.turnOrder.add(e.getId()));

        Map<String, Object> updatePayload = new HashMap<>();

        updatePayload.put("combatId", this.combatId);
        updatePayload.put("currentTurnOrder", new ArrayList<>(this.turnOrder));
        gameSession.publishUpdate("combatRoundStarted", updatePayload);
    }

    private void startNextTurn() {
        String nextEntityId = this.turnOrder.poll();

        if(nextEntityId == null) {
            initializeTurnOrder();
            nextEntityId = this.turnOrder.poll();
        }

        this.currentTurnEntityId = nextEntityId;

        Entity currentEntity = gameSession.getEntities().get(this.currentTurnEntityId);
        if(currentEntity == null || !currentEntity.isAlive()) {
            startNextTurn();
            return; //не должно быть
        }
        currentEntity.setCurrentAP(currentEntity.getMaxAP());

        Map<String, Object> updatePayload = new HashMap<>();

        updatePayload.put("combatId", this.combatId);
        updatePayload.put("currentTurnEntityId", this.currentTurnEntityId);
        gameSession.publishUpdate("combatNextTurn", updatePayload);

        if(currentEntity instanceof Monster) {
            return;
        }
    }

    private void checkCombatEnd() {
        CombatOutcome outcome = checkCombatOutcome();

        if(outcome != CombatOutcome.IN_PROGRESS) {
            if(outcome == CombatOutcome.PLAYERS_WON) {
                for(Entity entity : this.team1.values()) {
                    entity.setState(EntityStateType.EXPLORING);
                }
            } else if(outcome == CombatOutcome.MONSTERS_WON) {
                // монстры выиграли, мб похилить их, вернуть на свои позиции..или ничего не делать
            } else if(outcome == CombatOutcome.DRAW) {
                //мб очистка какая-то
            }
            notifyCombatEndListeners(outcome);
        }
    }

    private CombatOutcome checkCombatOutcome() {
        boolean playersAlive = this.team1.values().stream().anyMatch(Entity::isAlive);
        boolean monstersAlive = this.team2.values().stream().anyMatch(Entity::isAlive);
        if(!playersAlive && !monstersAlive)
            return CombatOutcome.DRAW;
        else if(!monstersAlive)
            return CombatOutcome.PLAYERS_WON;
        else if(!playersAlive)
            return CombatOutcome.MONSTERS_WON;
        else return CombatOutcome.IN_PROGRESS;
    }

    public void addListener(CombatEndListener combatEndListener) {
        this.endListeners.add(combatEndListener);
    }

    public void removeListener(CombatEndListener combatEndListener) {
        this.endListeners.remove(combatEndListener);
    }

    private void notifyCombatEndListeners(CombatOutcome combatOutcome) {
        for(CombatEndListener combatEndListener : this.endListeners) {
            combatEndListener.onCombatEnded(this, combatOutcome);
        }
    }

    @Override
    public void onEntityDied(Entity e) {
        this.team1.remove(e.getId());
        this.team2.remove(e.getId());
        this.turnOrder.remove(e.getId());

        gameSession.publishUpdate("entity_died", e.getId());
        checkCombatEnd();
    }
}
