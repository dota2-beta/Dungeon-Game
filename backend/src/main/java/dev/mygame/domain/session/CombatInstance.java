package dev.mygame.domain.session;

import dev.mygame.domain.event.CombatEventListener;
import dev.mygame.dto.websocket.event.*;
import dev.mygame.dto.websocket.response.AbilityCooldownDto;
import dev.mygame.enums.CombatOutcome;
import dev.mygame.enums.EntityStateType;
import dev.mygame.domain.event.DeathListener;
import dev.mygame.domain.model.Entity;
import dev.mygame.domain.model.Monster;
import dev.mygame.service.AIService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class CombatInstance implements DeathListener {
    private String combatId;
    private final AIService aiService;

    private Map<String, Set<Entity>> teams = new HashMap<>();

    private List<String> turnOrder = new ArrayList<>();
    private int currentTurnIndex = -1;
    private boolean isFinished = false;

    private Set<String> hasTakenFirstTurn = new HashSet<>();
    private final int apPerTurn;

    @EqualsAndHashCode.Exclude
    private final CombatEventListener eventListener;

    private static final Logger log = LoggerFactory.getLogger(CombatInstance.class);

    public CombatInstance(String combatId, List<Entity> initialParticipants, AIService aiService, int apPerTurn, CombatEventListener eventListener) {
        this.combatId = combatId;
        this.aiService = aiService;
        this.apPerTurn = apPerTurn;
        this.eventListener = eventListener;
        initializeTeamsAndListeners(initialParticipants);
    }

    public void start() {
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

    private void startNextTurn() {
        if (isFinished) return;
        for (int i = 0; i < turnOrder.size() + 1; i++) {
            currentTurnIndex++;
            if (currentTurnIndex >= turnOrder.size()) {
                initializeTurnOrder();
                currentTurnIndex = 0;
            }
            if (turnOrder.isEmpty()) {
                endCombatForAll();
                return;
            }
            Entity nextEntity = eventListener.getEntityById(getCurrentTurnEntityId());
            if (nextEntity != null && nextEntity.isAlive()) {
                beginTurnFor(nextEntity);
                return;
            }
        }

        log.warn("No alive entities found in turn order. Ending combat.");
        endCombatForAll();
    }

    private void beginTurnFor(Entity entity) {
        int currentAp = Math.min(
                entity.getCurrentAP() + this.apPerTurn,
                entity.getMaxAP()
        );
        entity.setCurrentAP(currentAp);
        entity.reduceAllCooldowns();

        List<AbilityCooldownDto> cooldowns = entity.getAbilities().stream()
                .map(ability -> AbilityCooldownDto.builder()
                        .abilityTemplateId(ability.getTemplate().getTemplateId())
                        .turnCooldown(ability.getTurnCooldown())
                        .cooldownEndTime(ability.getCooldownEndTime())
                        .build())
                .collect(Collectors.toList());
        CombatNextTurnEvent nextTurnEvent = CombatNextTurnEvent.builder()
                .combatId(this.combatId)
                .currentTurnEntityId(getCurrentTurnEntityId())
                .currentAP(entity.getCurrentAP())
                .abilityCooldowns(cooldowns)
                .build();
        eventListener.onCombatEvent(nextTurnEvent);

        if (entity instanceof Monster) {
            eventListener.scheduleAiTurn(entity.getId());
        } else {
            System.out.println("--- Turn Start: " + entity.getName() + " (Player Controlled) - WAITING FOR ACTION ---");
        }
    }

    /**
     * Завершает текущий ход и запускает следующий.
     * Этот метод предполагает, что все проверки (чей ход и т.д.) уже пройдены.
     */
    //TODO: добавить на фронтенд dto для окончания хода
    public void proceedToNextTurn() {
        if (isFinished)
            return;
        EntityTurnEndEvent combatEndTurnEvent =
                new EntityTurnEndEvent(getCurrentTurnEntityId());
        eventListener.onCombatEvent(combatEndTurnEvent);
        startNextTurn();
    }

    /**
     * Добавляет новых участников в уже идущий бой.
     * @param newParticipants Список новых сущностей для добавления.
     */
    public void addParticipantsToCombat(List<Entity> newParticipants) {
        initializeTeamsAndListeners(newParticipants);

        List<Entity> remainingInQueue = this.turnOrder.stream()
                .map(eventListener::getEntityById)
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
        eventListener.onCombatEvent(event);
    }

    public String getCurrentTurnEntityId() {
        if (currentTurnIndex >= 0 && currentTurnIndex < turnOrder.size()) {
            return turnOrder.get(currentTurnIndex);
        }
        return null;
    }

    @Override
    public void onEntityDied(Entity e) {
        log.info("CombatInstance: Entity {} died.", e.getName());
        EntityDiedEvent entityDiedEvent = new EntityDiedEvent(e.getId());
        eventListener.onCombatEvent(entityDiedEvent);

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

        eventListener.onCombatEnded(this.combatId, finalOutcome, winningTeamId, allParticipants);

        this.teams.clear();
        this.turnOrder.clear();
    }

    public void endCombatForAllByAgreement() {
        if (this.isFinished) return;
        this.isFinished = true;

        List<Entity> allParticipants = this.teams.values().stream()
                .flatMap(Set::stream)
                .toList();

        eventListener.onCombatEnded(this.combatId, CombatOutcome.END_BY_AGREEMENT, null, allParticipants);
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

    public List<Entity> getAliveEntities() {
        return this.teams.values().stream()
                .flatMap(Collection::stream)
                .filter(Entity::isAlive)
                .toList();
    }
}
