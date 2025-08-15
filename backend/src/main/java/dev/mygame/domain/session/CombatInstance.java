package dev.mygame.domain.session;

import dev.mygame.dto.websocket.event.*;
import dev.mygame.dto.websocket.response.AbilityCooldownDto;
import dev.mygame.enums.CombatOutcome;
import dev.mygame.enums.EntityStateType;
import dev.mygame.domain.event.CombatEndListener;
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
    private boolean isFinished = false;

    private Set<String> hasTakenFirstTurn = new HashSet<>();

    private List<CombatEndListener> endListeners = new ArrayList<>();

    private static final Logger log = LoggerFactory.getLogger(CombatInstance.class);

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

//    private void initializeTurnOrder(Entity initiator) {
//        List<Entity> otherParticipants = new ArrayList<>();
//        this.teams.values().forEach(team ->
//                team.stream()
//                        .filter(Entity::isAlive)
//                        .filter(entity -> !entity.getId().equals(initiator.getId()))
//                        .forEach(otherParticipants::add)
//        );
//
//        otherParticipants.sort(
//                Comparator.comparingInt(Entity::getInitiative).reversed()
//                        .thenComparing(Entity::getId)
//        );
//
//        this.turnOrder.clear();
//        this.turnOrder.add(initiator.getId());
//        otherParticipants.forEach(entity -> this.turnOrder.add(entity.getId()));
//
//        this.currentTurnIndex = 0;
//
//        log.info("--- [INITIATOR-BASED SORT] Final turn order: {}", this.turnOrder);
//    }

//    private void startNextTurn() {
//        printState("At start of startNextTurn");
//        if (this.currentTurnIndex != -1) {
//            this.currentTurnIndex++;
//        } else {
//            this.currentTurnIndex = 0;
//        }
//
//        if (currentTurnIndex >= this.turnOrder.size()) {
//            initializeTurnOrder();
//            this.currentTurnIndex = 0;
//        }
//
//        if (this.turnOrder.isEmpty()) {
//            endCombatForAll();
//            return;
//        }
//
//        String currentEntityId = getCurrentTurnEntityId();
//        Entity nextEntity = this.gameSession.getEntities().get(currentEntityId);
//
//        if (nextEntity == null || !nextEntity.isAlive()) {
//            startNextTurn();
//            return;
//        }
//        nextEntity.reduceAllCooldowns();
//
//        if (hasTakenFirstTurn.contains(currentEntityId)) {
//            int restoredAP = Math.min(
//                    nextEntity.getCurrentAP() + gameSession.getStandartEntityGameSettings().getDefaultEntityCurrentAp(),
//                    nextEntity.getMaxAP()
//            );
//            nextEntity.setCurrentAP(restoredAP);
//        } else {
//            final int startingAP = gameSession.getStandartEntityGameSettings().getDefaultEntityCurrentAp();
//            nextEntity.setCurrentAP(startingAP);
//            hasTakenFirstTurn.add(currentEntityId);
//        }
//        List<AbilityCooldownDto> currentAbilityCooldowns = nextEntity.getAbilities().stream()
//                .map(abilityInstance -> AbilityCooldownDto.builder()
//                        .turnCooldown(abilityInstance.getTurnCooldown())
//                        .cooldownEndTime(abilityInstance.getCooldownEndTime())
//                        .abilityTemplateId(abilityInstance.getTemplate().getTemplateId())
//                        .build())
//                .toList();
//
//
//        CombatNextTurnEvent nextTurnEvent = CombatNextTurnEvent.builder()
//                .combatId(this.combatId)
//                .currentTurnEntityId(getCurrentTurnEntityId())
//                .currentAP(nextEntity.getCurrentAP())
//                .abilityCooldowns(currentAbilityCooldowns)
//                .build();
//        this.gameSession.publishUpdate("combat_next_turn", nextTurnEvent);
//
//        if (nextEntity instanceof Monster) {
//            gameSession.getScheduler().schedule(() -> {
//                aiService.executeMonsterTurn((Monster) nextEntity, this.gameSession);
//            }, 1, TimeUnit.SECONDS);
//        } else {
//        }
//        printState("After selecting next entity in startNextTurn");
//    }

    private void startNextTurn() {
        if (isFinished) return;

        // Ищем следующего ЖИВОГО участника
        for (int i = 0; i < turnOrder.size() + 1; i++) { // Защита от бесконечного цикла
            currentTurnIndex++;

            // Если раунд закончился, начинаем новый
            if (currentTurnIndex >= turnOrder.size()) {
                initializeTurnOrder(); // Пересобираем очередь из живых
                currentTurnIndex = 0;
            }

            if (turnOrder.isEmpty()) {
                endCombatForAll();
                return;
            }

            Entity nextEntity = gameSession.getEntities().get(getCurrentTurnEntityId());
            if (nextEntity != null && nextEntity.isAlive()) {
                // Нашли! Запускаем его ход.
                beginTurnFor(nextEntity);
                return;
            }
        }

        // Если после полного круга не нашли живых - бой окончен
        log.warn("No alive entities found in turn order. Ending combat.");
        endCombatForAll();
    }

    private void beginTurnFor(Entity entity) {
        int currentAp = Math.min(
                entity.getCurrentAP() + gameSession.getStandartEntityGameSettings().getDefaultEntityCurrentAp(),
                entity.getMaxAP()
        );
        entity.setCurrentAP(currentAp);
        entity.reduceAllCooldowns();

        // Отправляем событие
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
        gameSession.publishEvent(nextTurnEvent);

        if (entity instanceof Monster) {
            gameSession.getScheduler().schedule(() -> {
                aiService.executeMonsterTurn((Monster) entity, this.gameSession);
            }, 1, TimeUnit.SECONDS);
        } else {
            System.out.println("--- Turn Start: " + entity.getName() + " (Player Controlled) - WAITING FOR ACTION ---");
        }
    }

    public void endCombatByAgreement() {

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
        gameSession.publishEvent(getCurrentTurnEntityId());
        startNextTurn();
    }

    /**
     * Добавляет новых участников в уже идущий бой.
     * @param newParticipants Список новых сущностей для добавления.
     */
    public void addParticipantsToCombat(List<Entity> newParticipants) {
        System.out.println("--- [GameSession] Adding participants to combat ---");
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
        gameSession.publishEvent(event);
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
        log.info("CombatInstance: Entity {} died.", e.getName());
        EntityDiedEvent entityDiedEvent = new EntityDiedEvent(e.getId());
        gameSession.publishEvent(entityDiedEvent);

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

    public boolean isEntityAlive(String entityId) {
        Entity entity = this.teams.values().stream()
                .flatMap(Collection::stream)
                .filter(e -> e.getId().equals(entityId))
                .findFirst()
                .orElse(null);
        if (entity == null) return false;
        return entity.isAlive();
    }

    public void endCombatForAllByAgreement() {
        if (this.isFinished) return;
        this.isFinished = true;

        List<Entity> allParticipants = this.teams.values().stream()
                .flatMap(Set::stream)
                .toList();

        notifyCombatEndListeners(CombatOutcome.END_BY_AGREEMENT, null, allParticipants);
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
