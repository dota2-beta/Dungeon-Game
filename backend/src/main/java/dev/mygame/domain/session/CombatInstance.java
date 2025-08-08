package dev.mygame.domain.session;

import dev.mygame.dto.websocket.response.AbilityCooldownDto;
import dev.mygame.dto.websocket.response.event.CombatNextTurnEvent;
import dev.mygame.dto.websocket.response.event.CombatParticipantsJoinedEvent;
import dev.mygame.dto.websocket.response.event.EntityTurnEndedEvent;
import dev.mygame.enums.CombatOutcome;
import dev.mygame.enums.EntityStateType;
import dev.mygame.domain.event.CombatEndListener;
import dev.mygame.domain.event.DeathListener;
import dev.mygame.domain.model.Entity;
import dev.mygame.domain.model.Monster;
import dev.mygame.domain.model.Player;
import dev.mygame.service.AIService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class CombatInstance implements DeathListener {
    private String combatId;

    @EqualsAndHashCode.Exclude
    private GameSession gameSession;

    private final AIService aiService;

    private Map<String, Set<Entity>> teams = new HashMap<>();

    private List<String> turnOrder = new ArrayList<>();
    private int currentTurnIndex = -1;
    private String currentTurnEntityId;

    private Set<String> hasTakenFirstTurn = new HashSet<>();

    private List<CombatEndListener> endListeners = new ArrayList<>();

    private boolean isFinished = false;

    private static final Logger log = LoggerFactory.getLogger(CombatInstance.class);

    public CombatInstance(String combatId, GameSession gameSession,
                          List<Entity> initialParticipants, Entity initiator, AIService aiService) {
        this.combatId = combatId;
        this.gameSession = gameSession;
        this.aiService = aiService;

        initializeTeamsAndListeners(initialParticipants);
        //initializeTurnOrder(initiator);
        initializeTurnOrder();

        startNextTurn();
    }

    public CombatInstance(String combatId, GameSession gameSession, List<Entity> initialParticipants, AIService aiService) {
        this.combatId = combatId;
        this.gameSession = gameSession;
        this.aiService = aiService;

        initializeTeamsAndListeners(initialParticipants);
        initializeTurnOrder();

        startNextTurn();
    }

    private void initializeTeamsAndListeners(List<Entity> participants) {
        for(Entity entity : participants) {
            entity.setState(EntityStateType.COMBAT);
            entity.addDeathListener(this);

            String teamId = entity.getTeamId();
            if(teamId == null)
                teamId = entity.getId();
            this.teams.computeIfAbsent(teamId, k -> new HashSet<>()).add(entity);
        }
    }

    /**
     * Инициализирует очередь ходов, когда нет явного инициатора.
     * Все участники сортируются по инициативе.
     */

    private void initializeTurnOrder() {
        List<Entity> allParticipants = new ArrayList<>();
        this.teams.values().forEach(team ->
                team.stream()
                        .filter(Entity::isAlive)
                        .forEach(allParticipants::add)
        );
        allParticipants.sort(
                Comparator.comparingInt(Entity::getInitiative).reversed()
                        .thenComparing(Entity::getId)
        );
        this.turnOrder.clear();
        allParticipants.forEach(entity -> this.turnOrder.add(entity.getId()));
        this.currentTurnIndex = -1;
        log.info("Initialized new turn order for the round: {}", this.turnOrder);
    }



    /**
     * Инициализирует очередь ходов, когда есть явный инициатор.
     * Инициатор ставится первым, остальные сортируются по инициативе.
     */

    private void initializeTurnOrder(Entity initiator) {
        List<Entity> otherParticipants = new ArrayList<>();
        this.teams.values().forEach(team ->
                team.stream()
                        .filter(Entity::isAlive)
                        .filter(entity -> !entity.getId().equals(initiator.getId()))
                        .forEach(otherParticipants::add)
        );

        otherParticipants.sort(
                Comparator.comparingInt(Entity::getInitiative).reversed()
                        .thenComparing(Entity::getId)
        );

        this.turnOrder.clear();
        this.turnOrder.add(initiator.getId());
        otherParticipants.forEach(entity -> this.turnOrder.add(entity.getId()));

        this.currentTurnIndex = 0;

        log.info("--- [INITIATOR-BASED SORT] Final turn order: {}", this.turnOrder);
    }

    private void startNextTurn() {
        if (this.currentTurnIndex != -1) {
            this.currentTurnIndex++;
        } else {
            this.currentTurnIndex = 0;
        }

//        this.teams.values().stream()
//                .flatMap(Collection::stream)
//                .forEach(Entity::reduceAllCooldowns);

        if (currentTurnIndex >= this.turnOrder.size()) {
            initializeTurnOrder();
            this.currentTurnIndex = 0;
        }

        if (this.turnOrder.isEmpty()) {
            endCombatForAll();
            return;
        }

        String currentEntityId = getCurrentTurnEntityId();
        Entity nextEntity = this.gameSession.getEntities().get(currentEntityId);

        if (nextEntity == null || !nextEntity.isAlive()) {
            startNextTurn();
            return;
        }
        nextEntity.reduceAllCooldowns();

        if (hasTakenFirstTurn.contains(currentEntityId)) {
            int restoredAP = Math.min(
                    nextEntity.getCurrentAP() + gameSession.getGameSettings().getDefaultEntityCurrentAp(),
                    nextEntity.getMaxAP()
            );
            nextEntity.setCurrentAP(restoredAP);
        } else {
            final int startingAP = gameSession.getGameSettings().getDefaultEntityCurrentAp();
            nextEntity.setCurrentAP(startingAP);
            hasTakenFirstTurn.add(currentEntityId);
        }
        List<AbilityCooldownDto> currentAbilityCooldowns = nextEntity.getAbilities().stream()
                .map(abilityInstance -> AbilityCooldownDto.builder()
                        .turnCooldown(abilityInstance.getTurnCooldown())
                        .cooldownEndTime(abilityInstance.getCooldownEndTime())
                        .abilityTemplateId(abilityInstance.getTemplate().getTemplateId())
                        .build())
                .toList();


        CombatNextTurnEvent nextTurnEvent = CombatNextTurnEvent.builder()
                .combatId(this.combatId)
                .currentTurnEntityId(getCurrentTurnEntityId())
                .currentAP(nextEntity.getCurrentAP())
                .abilityCooldowns(currentAbilityCooldowns)
                .build();
        this.gameSession.publishUpdate("combat_next_turn", nextTurnEvent);

        if (nextEntity instanceof Monster) {
            System.out.println("--- Turn Start: " + nextEntity.getName() + " (AI Controlled) ---");
            gameSession.getScheduler().schedule(() -> {
                aiService.executeMonsterTurn((Monster) nextEntity, this.gameSession);
            }, 1, TimeUnit.SECONDS);
        } else {
            System.out.println("--- Turn Start: " + nextEntity.getName() + " (Player Controlled) ---");
        }
    }

    /**
     * Завершает текущий ход и запускает следующий.
     * Этот метод предполагает, что все проверки (чей ход и т.д.) уже пройдены.
     */
    public void proceedToNextTurn() {
        EntityTurnEndedEvent event = new EntityTurnEndedEvent(getCurrentTurnEntityId());
        this.gameSession.publishUpdate("entity_turn_ended", event);

        startNextTurn();
    }

    /**
     * Добавляет новых участников в уже идущий бой.
     * @param newParticipants Список новых сущностей для добавления.
     */
    public void addParticipantsToCombat(List<Entity> newParticipants) {
        initializeTeamsAndListeners(newParticipants);

        List<Entity> remainingInQueue = this.turnOrder.stream()
                .map(id -> gameSession.getEntities().get(id))
                .filter(Objects::nonNull)
                .toList();
        List<Entity> turnTakersToResort = new ArrayList<>(remainingInQueue);
        turnTakersToResort.addAll(newParticipants);

        turnTakersToResort.sort(Comparator.comparing(Entity::getInitiative).reversed());

        turnOrder.clear();
        turnTakersToResort.forEach(entity -> turnOrder.add(entity.getId()));

        Set<String> newParticipantIds = newParticipants.stream()
                .map(Entity::getId)
                .collect(Collectors.toSet());

        CombatParticipantsJoinedEvent event = new CombatParticipantsJoinedEvent(
                this.combatId,
                newParticipantIds,
                new ArrayList<>(this.turnOrder)
        );
        gameSession.publishUpdate("combat_participants_joined", event);
    }

    public void addListener(CombatEndListener combatEndListener) {
        this.endListeners.add(combatEndListener);
    }

    public void removeListener(CombatEndListener combatEndListener) {
        this.endListeners.remove(combatEndListener);
    }

    private void notifyCombatEndListeners(CombatOutcome combatOutcome, String winningTeamId, List<Entity> allParticipants) {
        if (this.endListeners == null || this.endListeners.isEmpty()) {
            return;
        }
        List<CombatEndListener> listeners = new ArrayList<>(this.endListeners);
        for (CombatEndListener listener : listeners) {
            listener.onCombatEnded(this, combatOutcome, winningTeamId, allParticipants);
        }
    }

    public String getCurrentTurnEntityId() {
        if (currentTurnIndex >= 0 && currentTurnIndex < turnOrder.size()) {
            return turnOrder.get(currentTurnIndex);
        }
        return null;
    }

    @Override
    public void onEntityDied(Entity e) {
        log.info("CombatInstance: Entity {} died. Updating combat state.", e.getName());
        this.turnOrder.remove(e.getId());
        gameSession.publishUpdate("entity_died", e.getId());

        long remainingTeamsCount = teams.values().stream()
                .filter(t -> t.stream().anyMatch(Entity::isAlive))
                .count();

        if (remainingTeamsCount <= 1) {
            endCombatForAll();
        }
    }

    private void endCombatForAll() {
        if (this.isFinished) return;
        this.isFinished = true;

        List<Entity> allParticipants = this.teams.values().stream()
                .flatMap(Set::stream)
                .toList();

        String winningTeamId = findWinningTeam();
        CombatOutcome finalOutcome = (winningTeamId != null) ? CombatOutcome.VICTORY : CombatOutcome.DEFEAT;

        notifyCombatEndListeners(finalOutcome, winningTeamId, allParticipants);

        this.teams.clear();
        this.turnOrder.clear();
    }

    private String findWinningTeam() {
        List<String> winningTeam = teams.entrySet().stream()
                .filter(team -> team.getValue().stream().anyMatch(Entity::isAlive))
                .map(Map.Entry::getKey)
                .toList();

        if(winningTeam.size() == 1)
            return winningTeam.get(0);
        return null;
    }
}
