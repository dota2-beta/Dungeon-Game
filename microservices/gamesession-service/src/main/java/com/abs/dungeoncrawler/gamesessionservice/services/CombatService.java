package com.abs.dungeoncrawler.gamesessionservice.services;

import com.abs.dungeonCrawler.eventcontracts.Dto.EntityStateDto;
import com.abs.dungeonCrawler.eventcontracts.enums.CombatOutcome;
import com.abs.dungeonCrawler.eventcontracts.eventDto.*;
import com.abs.dungeoncrawler.gamesessionservice.config.StandartEntityGameSettings;
import com.abs.dungeoncrawler.gamesessionservice.domain.Combat;
import com.abs.dungeoncrawler.gamesessionservice.domain.GameSession;
import com.abs.dungeoncrawler.gamesessionservice.domain.model.Entity;
import com.abs.dungeonCrawler.eventcontracts.enums.EntityState;
import com.abs.dungeoncrawler.gamesessionservice.domain.model.GameMapObject;
import com.abs.dungeoncrawler.gamesessionservice.domain.model.Monster;
import com.abs.dungeoncrawler.gamesessionservice.domain.model.Player;
import com.abs.dungeoncrawler.gamesessionservice.exception.GameActionException;
import com.abs.dungeoncrawler.gamesessionservice.kafka.KafkaEventPublisher;
import com.abs.dungeoncrawler.gamesessionservice.mapper.EntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CombatService {
    private final StandartEntityGameSettings settings;
    private final KafkaEventPublisher publisher;
    private final FactionService factionService;
    private final EntityMapper entityMapper;

    private final AIService aiService;

    /**
     * Создает и запускает новый бой с указанными участниками в указанной сессии
     * <p>
     * Метод инициализирует {@link Combat}, переводит способности участников
     * в боевой режим (конвертирует кулдауны), добавляет бой в список активных
     * и оповещает всех клиентов о его начале через событие {@link CombatStartedEvent}.
     *
     * @param participants Все сущности, вступающие в бой.
     * @param session Сессия, в рамках которой запускается новый бой.
     */

    public void startCombat(GameSession session, List<Entity> participants) {
        String combatId = UUID.randomUUID().toString();
        Combat combat = Combat.builder()
                        .id(combatId)
                        .participantIds(
                                participants.stream()
                                .map(GameMapObject::getId)
                                .collect(Collectors.toSet())
                        )
                        .build();
        initializeTurnOrder(session, combat);
        participants.forEach(p -> p.setState(EntityState.COMBAT));
        session.addCombat(combat);

        List<EntityStateDto> combatants = participants.stream()
                .map(entityMapper::toEntityState)
                .toList();

        Map<String, Set<String>> teamsMap = participants.stream()
                .collect(Collectors.groupingBy(
                        Entity::getTeamId,
                        Collectors.mapping(Entity::getId, Collectors.toSet())
                ));
        List<CombatTeamDto> combatTeamDtos = teamsMap.entrySet().stream()
                .map(entry -> CombatTeamDto.builder()
                        .teamId(entry.getKey())
                        .memberIds(entry.getValue())
                        .build())
                .toList();

        CombatStartedEvent event = CombatStartedEvent.builder()
                .sessionId(session.getSessionId())
                .combatId(combatId)
                .teams(combatTeamDtos)
                .initialTurnOrder(combat.getTurnOrder())
                .combatants(combatants)
                .build();
        publisher.publishCombatStartedEvent(event);

        startNextTurn(session, combat);
    }

    public void startNextTurn(GameSession session, Combat combat) {
        List<String> turnOrder = combat.getTurnOrder();
        if (turnOrder.isEmpty()) return;

        int nextIndex = combat.getCurrentTurnIndex() + 1;
        if (nextIndex >= turnOrder.size()) {
            nextIndex = 0;
        }
        combat.setCurrentTurnIndex(nextIndex);

        String nextEntityId = turnOrder.get(nextIndex);
        Entity nextEntity = session.getEntityById(nextEntityId);

        if (nextEntity == null || nextEntity.isDead()) {
            startNextTurn(session, combat);
            return;
        }

        int recovery = settings.getDefaultEntityCurrentAp();
        int maxAp = nextEntity.getMaxAP();

        nextEntity.setCurrentAP(Math.min(nextEntity.getCurrentAP() + recovery, maxAp));

        // TODO: reduceCooldowns()

        CombatTurnChangedEvent event = CombatTurnChangedEvent.builder()
                .sessionId(session.getSessionId())
                .combatId(combat.getId())
                .activeEntityId(nextEntityId)
                .currentAP(nextEntity.getCurrentAP())
                //.isMonster(nextEntity instanceof Monster) // Флаг для AI
                .build();

        publisher.publishCombatTurnChangedEvent(event);

        if (nextEntity instanceof Monster) {
            log.info("Turn passed to Monster {}. Scheduling AI...", nextEntity.getId());
            aiService.scheduleTurn(session.getSessionId(), nextEntity.getId());
        }
    }

    public List<Entity> findNearbyAlliesAndEnemies(GameSession session,
                                                   Entity attacker,
                                                   Entity target
    ) {
        int combatJoinRadius = settings.getDefaultCheckRadius();
        return session.getEntities().values().stream()
                .filter(entity -> entity.getState() == EntityState.EXPLORING)
                .filter(entity -> entity.getPosition().distanceTo(attacker.getPosition()) <= combatJoinRadius ||
                        entity.getPosition().distanceTo(target.getPosition()) <= combatJoinRadius)
                .filter(entity -> Objects.equals(entity.getTeamId(), attacker.getTeamId()) ||
                        Objects.equals(entity.getTeamId(), target.getTeamId()) ||
                        entity.equals(attacker) || entity.equals(target))
                .toList();
    }

    public void handleEntityDeath(GameSession session, String entityId) {
        Optional<Combat> combatOpt = session.findCombatByEntityId(entityId);
        if (combatOpt.isEmpty()) return;

        Combat combat = combatOpt.get();

        long aliveTeamsCount = combat.getParticipantIds().stream()
                .map(session::getEntityById)
                .filter(Objects::nonNull)
                .filter(e -> !e.isDead())
                .map(Entity::getTeamId)
                .distinct()
                .count();

        if (aliveTeamsCount == 1) {
            endCombat(session, combat, CombatOutcome.SOME_TEAM_WON);
        } else if(aliveTeamsCount < 1) {
            endCombat(session, combat, CombatOutcome.DRAW);
        } else {
            removeParticipantFromTurnOrder(combat, entityId);
        }
    }

    public void endCombat(GameSession session, Combat combat, CombatOutcome combatResult) {
        combat.getParticipantIds().stream()
                .map(session::getEntityById)
                .filter(Objects::nonNull) // На всякий случай
                .forEach(e -> e.setState(EntityState.EXPLORING));

        session.removeCombat(combat.getId());
        log.info("Combat {} ended. Outcome: {}", combat.getId(), combatResult);

        String winningTeamId = null;

        if (combatResult == CombatOutcome.SOME_TEAM_WON) {
            winningTeamId = combat.getParticipantIds().stream()
                    .map(session::getEntityById)
                    .filter(Objects::nonNull)
                    .filter(e -> !e.isDead())
                    .map(Entity::getTeamId)
                    .findFirst()
                    .orElse(null);
        }

        CombatEndedEvent event = CombatEndedEvent.builder()
                .sessionId(session.getSessionId())
                .combatId(combat.getId())
                .outcome(combatResult)
                .winningTeamId(winningTeamId)
                .build();

        publisher.publishCombatEndedEvent(event);
    }

    public void requestEndTurn(GameSession session, String entityId) {
        Combat combat = session.findCombatByEntityId(entityId)
                .orElseThrow(() -> new GameActionException("Entity is not in combat", "NOT_IN_COMBAT"));

        String currentActorId = combat.getTurnOrder().get(combat.getCurrentTurnIndex());
        if (!currentActorId.equals(entityId)) {
            throw new GameActionException("It is not your turn!", "NOT_YOUR_TURN");
        }

        startNextTurn(session, combat);
    }

    private void initializeTurnOrder(GameSession session, Combat combat) {
        List<Entity> allParticipants = new ArrayList<>(
                combat.getParticipantIds().stream()
                .map(session::getEntityById)
                .toList()
        );
        allParticipants.sort(
                Comparator.comparingInt(Entity::getInitiative).reversed()
                .thenComparing(Entity::getId)
        );

        List<String> newOrderIds = allParticipants.stream()
                .map(Entity::getId)
                .toList();

        combat.setTurnOrder(newOrderIds);
        combat.setCurrentTurnIndex(-1);
        log.info("Initialized new turn order for the round: {}", combat.getTurnOrder());
    }

    /**
     * Метод для проверки необходимости начала боя после перемещения.
     * <p>
     * Бой начинается, когда сущность заходит в зону "агра".
     * @param movedEntity ID сущности, которая совершила перемещение
     */
    public void checkForCombatStart(GameSession session, Entity movedEntity) {
        if(movedEntity.getState() == EntityState.COMBAT)
            return;

        final int MAX_CHECK_RADIUS = 10;

        List<Entity> nearbyEntities = findAllEntitiesInRadius(session, movedEntity.getId(), MAX_CHECK_RADIUS);
        if (nearbyEntities.isEmpty())
            return;

        boolean isEnemyPresent = false;
        String existingCombatId = null;
        for(Entity nearbyEntity : nearbyEntities)
            if(factionService.areEnemies(nearbyEntity, movedEntity)) {
                if(nearbyEntity.getState() == EntityState.EXPLORING) {
                    int distance = movedEntity.getPosition().distanceTo(nearbyEntity.getPosition());
                    if (distance <= movedEntity.getAggroRadius() || distance <= nearbyEntity.getAggroRadius()) {
                        // бой будет начинаться для ВСЕХ юнитов, которые находятся рядом, а не только для врагов
                        // потом можно создать метод для окончания боя по согласию всех участников
                        isEnemyPresent = true;
                    }
                } else {
                    Optional<Combat> combat = session.findCombatByEntityId(nearbyEntity.getId());
                    if (combat.isPresent()) {
                        existingCombatId = combat.get().getId();
                        break;
                    }
                }
            }
        if (!isEnemyPresent && existingCombatId == null) {
            return;
        }

        List<Entity> joiningGroup = new ArrayList<>(
                nearbyEntities.stream()
                .filter(e -> e.getState() == EntityState.EXPLORING)
                .toList()
        );
        joiningGroup.add(movedEntity);

        if(existingCombatId != null) {
            log.info("{}'s group joins an existing combat!", movedEntity.getName());
            //convertAbilityCooldownByEntityStateType(EntityState.COMBAT, joiningGroup);
            addParticipantsToCombat(session, existingCombatId, joiningGroup);
        } else {
            startCombat(session, joiningGroup);
        }
    }

    public void addParticipantsToCombat(GameSession session, String combatId, List<Entity> newParticipants) {
        Combat combat = session.getCombat(combatId);

        Set<String> newIds = newParticipants.stream()
                .map(Entity::getId)
                .collect(Collectors.toSet());
        combat.getParticipantIds().addAll(newIds);

        newParticipants.forEach(e -> e.setState(EntityState.COMBAT));

        recalculateTurnOrder(session, combat);

        List<EntityStateDto> newCombatantsDtos = newParticipants.stream()
                .map(entityMapper::toEntityState)
                .toList();
        String activeEntityId = null;
        if (combat.getCurrentTurnIndex() >= 0 && combat.getCurrentTurnIndex() < combat.getTurnOrder().size()) {
            activeEntityId = combat.getTurnOrder().get(combat.getCurrentTurnIndex());
        }

        CombatParticipantsJoinedEvent event = CombatParticipantsJoinedEvent.builder()
                .sessionId(session.getSessionId())
                .combatId(combat.getId())
                .turnOrder(combat.getTurnOrder())
                .participants(newCombatantsDtos)
                .activeEntityId(activeEntityId)
                .build();
        publisher.publishCombatParticipantsJoinedEvent(event);
    }

    /**
     * Проверяет, не заагрил ли боец кого-то нового своим перемещением.
     * Если находит мирных врагов — добавляет в бой.
     * Если находит врагов из другого боя — сливает бои.
     */
    public void checkAggroDuringCombat(GameSession session, Entity mover) {
        Optional<Combat> combatOpt = session.findCombatByEntityId(mover.getId());
        if (combatOpt.isEmpty()) return;
        Combat currentCombat = combatOpt.get();

        List<Entity> neighbors = session.getEntities().values().stream()
                .filter(e -> !e.getId().equals(mover.getId())) // Не я сам
                .filter(e -> factionService.areEnemies(mover, e)) // Враг
                .filter(e -> e.getPosition().distanceTo(mover.getPosition()) <= e.getAggroRadius()) // В радиусе агра врага
                .toList();

        List<Entity> entitiesToJoin = new ArrayList<>();

        for (Entity neighbor : neighbors) {
            if (neighbor.getState() == EntityState.EXPLORING) {
                entitiesToJoin.add(neighbor);
            }
            else if (neighbor.getState() == EntityState.COMBAT) {
                Optional<Combat> otherCombatOpt = session.findCombatByEntityId(neighbor.getId());

                if (otherCombatOpt.isPresent()) {
                    Combat otherCombat = otherCombatOpt.get();
                    if (!otherCombat.getId().equals(currentCombat.getId())) {
                        log.info("Aggro check triggered MERGE: Combat {} absorbs {}", currentCombat.getId(), otherCombat.getId());
                        mergeCombats(session, currentCombat, otherCombat);
                    }
                }
            }
        }

        if (!entitiesToJoin.isEmpty()) {
            log.info("Entity {} pulled {} new enemies into combat {}", mover.getName(), entitiesToJoin.size(), currentCombat.getId());
            addParticipantsToCombat(session, currentCombat.getId(), entitiesToJoin);
        }
    }

    /**
     * Сливает два боя в один.
     * @param master Бой, который останется
     * @param slave Бой, который исчезнет
     */
    public void mergeCombats(GameSession session, Combat master, Combat slave) {
        if (master.getId().equals(slave.getId())) {
            log.warn("Attempted to merge combat {} into itself.", master.getId());
            return;
        }

        log.info("Merging combat {} (Slave) into combat {} (Master)", slave.getId(), master.getId());

        List<Entity> slaveParticipants = slave.getParticipantIds().stream()
                .map(session::getEntityById)
                .filter(Objects::nonNull) // На случай, если кто-то вышел/удален
                .toList();

        session.removeCombat(slave.getId());

        CombatEndedEvent endEvent = CombatEndedEvent.builder()
                .sessionId(session.getSessionId())
                .combatId(slave.getId())
                .outcome(CombatOutcome.MERGED)
                .winningTeamId(null)
                .build();

        publisher.publishCombatEndedEvent(endEvent);

        addParticipantsToCombat(session, master.getId(), slaveParticipants);
    }

    // Peace
    public void proposePeace(GameSession session, String userId) {
        Player initiator = session.getPlayerByUserId(userId)
                .orElseThrow(() -> new GameActionException("Player not found", "PLAYER_NOT_FOUND"));

        Combat combat = session.findCombatByEntityId(initiator.getId())
                .orElseThrow(() -> new GameActionException("Not in combat", "NOT_IN_COMBAT"));

        boolean hasMonsters = combat.getParticipantIds().stream()
                .map(session::getEntityById)
                .anyMatch(e -> e instanceof Monster && !e.isDead());

        if (hasMonsters) {
            throw new GameActionException("Cannot propose peace while monsters are alive!", "MONSTERS_ALIVE");
        }

        if (combat.getPeaceVotes() != null) {
            throw new GameActionException("Peace vote already in progress", "VOTE_IN_PROGRESS");
        }

        combat.setPeaceVotes(new ConcurrentHashMap<>());
        combat.getPeaceVotes().put(initiator.getId(), true);

        publisher.publishPeaceProposalEvent(PeaceProposalEvent.builder()
                .sessionId(session.getSessionId())
                .combatId(combat.getId())
                .initiatorName(initiator.getName())
                .initiatorId(initiator.getId())
                .build());

        // если инициатор один в бою (сам с собой?), сразу заканчиваем
        checkPeaceResult(session, combat);
    }

    public void respondToPeace(GameSession session, String userId, boolean accept) {
        Player responder = session.getPlayerByUserId(userId).orElseThrow();
        Combat combat = session.findCombatByEntityId(responder.getId()).orElseThrow();

        if (combat.getPeaceVotes() == null) {
            throw new GameActionException("No active peace proposal", "NO_VOTE");
        }

        if (!accept) {
            cancelPeaceVote(session, combat);
        } else {
            combat.getPeaceVotes().put(responder.getId(), true);
            checkPeaceResult(session, combat);
        }
    }

    private void checkPeaceResult(GameSession session, Combat combat) {
        List<String> alivePlayerIds = combat.getParticipantIds().stream()
                .map(session::getEntityById)
                .filter(e -> e instanceof Player && !e.isDead())
                .map(Entity::getId)
                .toList();

        boolean allAgreed = alivePlayerIds.stream()
                .allMatch(id -> combat.getPeaceVotes().getOrDefault(id, false));

        if (allAgreed) {
            combat.setPeaceVotes(null);

            publisher.publishPeaceResultEvent(PeaceResultEvent.builder()
                    .sessionId(session.getSessionId())
                    .combatId(combat.getId())
                    .success(true)
                    .build());

            endCombat(session, combat, CombatOutcome.END_BY_AGREEMENT);
        }
    }

    private void cancelPeaceVote(GameSession session, Combat combat) {
        combat.setPeaceVotes(null);

        publisher.publishPeaceResultEvent(PeaceResultEvent.builder()
                .sessionId(session.getSessionId())
                .combatId(combat.getId())
                .success(false)
                .build());
    }

    private void recalculateTurnOrder(GameSession session, Combat combat) {
        String currentActorId = null;
        if (combat.getCurrentTurnIndex() >= 0 && combat.getCurrentTurnIndex() < combat.getTurnOrder().size()) {
            currentActorId = combat.getTurnOrder().get(combat.getCurrentTurnIndex());
        }

        List<Entity> allParticipants = combat.getParticipantIds().stream()
                .map(session::getEntityById)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(Entity::getInitiative).reversed().thenComparing(Entity::getId))
                .toList();

        List<String> newOrder = allParticipants.stream().map(Entity::getId).toList();
        combat.setTurnOrder(new ArrayList<>(newOrder));

        if (currentActorId != null) {
            int newIndex = newOrder.indexOf(currentActorId);
            // Если текущий актер всё еще в списке (не умер), ставим индекс на него
            // Если вдруг его нет (странно, но вдруг), ставим 0 или обрабатываем конец хода
            combat.setCurrentTurnIndex(Math.max(0, newIndex));
        } else {
            combat.setCurrentTurnIndex(0);
        }

        log.info("Turn order recalculated: {}", combat.getTurnOrder());
    }

    private List<Entity> findAllEntitiesInRadius(GameSession session, String entityId, int searchingRadius) {
        Map<String, Entity> entities = session.getEntities();
        Entity entity = entities.get(entityId);
        return entities.values().stream()
                .filter(e -> !e.getId().equals(entity.getId()) &&
                        entity.getPosition().distanceTo(e.getPosition()) <= searchingRadius)
                .toList();
    }

    private void removeParticipantFromTurnOrder(Combat combat, String entityId) {
        List<String> order = combat.getTurnOrder();
        int indexToRemove = order.indexOf(entityId);

        if (indexToRemove == -1) {
            return;
        }

        order.remove(indexToRemove);

        // Если удалили кого-то, кто стоял в очереди перед текущим активным персонажем,
        // то индекс текущего персонажа сместился влево.
        if (indexToRemove < combat.getCurrentTurnIndex()) {
            combat.setCurrentTurnIndex(combat.getCurrentTurnIndex() - 1);
        }

        // если indexToRemove == currentTurnIndex,
        // то TODO:мб надо прерывать ход.

    }

    // первый аргумент - тип боя, В КОТОРЫЙ нужно перевести КД
//    private void convertAbilityCooldownByEntityStateType(EntityState entityStateType, List<Entity> participants) {
//        for(Entity participant : participants) {
//            for (AbilityInstance ability : participant.getAbilities()) {
//                if (entityStateType == EntityStateType.COMBAT) {
//                    if (ability.getCooldownEndTime() > System.currentTimeMillis()) {
//                        long timeLeftMs = ability.getCooldownEndTime() - System.currentTimeMillis();
//                        long secondsLeft = timeLeftMs / 1000;
//                        int turnsLeft = (int) Math.ceil((double) secondsLeft / 10.0);
//
//                        ability.setTurnCooldown(Math.max(turnsLeft, 0));
//                        ability.setCooldownEndTime(0);
//                    }
//                } else if (ability.getTurnCooldown() > 0) {
//                    long secondsToWait = ability.getTurnCooldown() * 10L;
//                    long newEndTime = System.currentTimeMillis() + secondsToWait * 1000;
//                    ability.setCooldownEndTime(Math.max(newEndTime, 0));
//                    ability.setTurnCooldown(0);
//                }
//            }
//            if (participant instanceof Player) {
//                publishCasterStateUpdate(participant);
//            }
//        }
//    }
}
