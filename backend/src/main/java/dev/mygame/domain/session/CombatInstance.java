package dev.mygame.domain.session;

import dev.mygame.dto.websocket.response.event.CombatNextTurnEvent;
import dev.mygame.dto.websocket.response.event.CombatParticipantsJoinedEvent;
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
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class CombatInstance implements DeathListener {
    private String combatId;
    private GameSession gameSession;

    private Map<String, Set<Entity>> teams = new HashMap<>();

    private Queue<String> turnOrder = new LinkedList<>();;
    private String currentTurnEntityId;

    private List<CombatEndListener> endListeners;

    public CombatInstance(String combatId, GameSession gameSession,
                          List<Entity> initialParticipants, Entity initiator) {
        this.combatId = combatId;
        this.gameSession = gameSession;

        initializeTeamsAndListeners(initialParticipants);
        initializeTurnOrder(initiator);

        startNextTurn();
    }

    public CombatInstance(String combatId, GameSession gameSession, List<Entity> initialParticipants) {
        this.combatId = combatId;
        this.gameSession = gameSession;

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

    private CombatOutcome checkCombatOutcomeForTeam(String teamId) {
        Set<Entity> myTeam = this.teams.get(teamId);

        boolean isTeamAlive = (myTeam != null) && myTeam.stream().anyMatch(entity -> !entity.isDead());
        if(!isTeamAlive)
            return CombatOutcome.DEFEAT;

        boolean isOtherTeamsAlive = this.teams.entrySet().stream()
                .filter(team -> team.getKey() != teamId)
                .anyMatch(team -> team.getValue().stream().anyMatch(entity -> !entity.isDead()));
        if(!isOtherTeamsAlive)
            return CombatOutcome.VICTORY;

        return CombatOutcome.IN_PROGRESS;
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

        // Просто сортируем всех по инициативе
        allParticipants.sort(Comparator.comparingInt(Entity::getInitiative).reversed());

        this.turnOrder.clear();
        allParticipants.forEach(entity -> this.turnOrder.add(entity.getId()));
    }

    /**
     * Инициализирует очередь ходов, когда есть явный инициатор.
     * Инициатор ставится первым, остальные сортируются по инициативе.
     */
    private void initializeTurnOrder(Entity initiator) {
        List<Entity> otherParticipants = new ArrayList<>();
        this.teams.values().forEach(team -> {
            team.stream()
                    .filter(Entity::isAlive)
                    .filter(entity -> !entity.getId().equals(initiator.getId()))
                    .forEach(otherParticipants::add);
        });
        otherParticipants.sort(Comparator.comparing(Entity::getInitiative).reversed());

        this.turnOrder.clear();
        this.turnOrder.add(initiator.getId());
        otherParticipants.forEach(entity -> this.turnOrder.add(entity.getId()));

        this.currentTurnEntityId = initiator.getId();
        initiator.setInitiative(initiator.getMaxAP());
    }

    private void startNextTurn() {
        if(this.turnOrder.isEmpty()) {
            initializeTurnOrder();
        }

        String nextEntityId = this.turnOrder.poll();

        Entity currentEntity = this.gameSession.getEntities().get(this.currentTurnEntityId);
        if(currentEntity == null || !currentEntity.isAlive()) {
            startNextTurn();
            return; //не должно быть
        }
        this.currentTurnEntityId = nextEntityId;
        currentEntity.setCurrentAP(currentEntity.getMaxAP());

        CombatNextTurnEvent nextTurnEvent = new CombatNextTurnEvent(
                this.combatId,
                this.currentTurnEntityId,
                currentEntity.getCurrentAP() // Передаем актуальное количество ОД
        );

        this.gameSession.publishUpdate("combat_next_turn", nextTurnEvent);


        if (currentEntity instanceof Monster) {
            // логика ИИ для монстров
        }
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

    private void notifyCombatEndListeners(CombatOutcome combatOutcome, String forTeamId) {
        if (this.endListeners == null || this.endListeners.isEmpty()) {
            return;
        }

        List<CombatEndListener> listeners = new ArrayList<>(this.endListeners);

        for (CombatEndListener listener : listeners) {
            listener.onCombatEnded(this, combatOutcome, forTeamId);
        }
    }

    @Override
    public void onEntityDied(Entity e) {
        String myTeamId = e.getTeamId();
        this.teams.get(myTeamId).remove(e);

        this.turnOrder.remove(e.getId());
        gameSession.publishUpdate("entity_died", e.getId());

        Set<String> allTeamIds = new HashSet<>(teams.keySet());
        for (String teamId : allTeamIds) {
            CombatOutcome outcome = checkCombatOutcomeForTeam(teamId);

            if (outcome != CombatOutcome.IN_PROGRESS) {
                Set<Entity> teamMembers = teams.get(teamId);
                if (teamMembers == null) continue;

                notifyCombatEndListeners(outcome, teamId);

                // Меняем состояние выживших членов команды
                if (outcome == CombatOutcome.VICTORY) {
                    teamMembers.forEach(member -> {
                        if (member.isAlive()) {
                            member.setState(EntityStateType.EXPLORING);
                        }
                    });
                }
            }
        }
        long remainingTeamsCount = teams.values().stream()
                .filter(team -> team.stream().anyMatch(Entity::isAlive))
                .count();

        if (remainingTeamsCount <= 1) {
            endCombatForAll();
        }
    }

    private void endCombatForAll() {
        this.teams.values().forEach(team -> {
            team.forEach(entity -> {
                entity.removeDeathListener(this);
                if(entity.isAlive())
                    entity.setState(EntityStateType.EXPLORING);
            });
        });

        this.teams.clear();
        this.turnOrder.clear();

        String winningTeamId = findWinningTeam();

        if(winningTeamId != null) {
            notifyCombatEndListeners(CombatOutcome.VICTORY, winningTeamId);
        } else {
            // ...
        }

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
