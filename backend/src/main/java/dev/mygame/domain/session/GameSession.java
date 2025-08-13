package dev.mygame.domain.session;

//import dev.mygame.game.model.map.Map;
import dev.mygame.config.StandartEntityGameSettings;
import dev.mygame.config.WebSocketDestinations;
import dev.mygame.domain.model.Entity;
import dev.mygame.domain.model.GameObject;
import dev.mygame.domain.model.Monster;
import dev.mygame.domain.model.Player;
import dev.mygame.domain.model.map.*;
import dev.mygame.dto.websocket.event.*;
import dev.mygame.dto.websocket.request.PeaceProposalEvent;
import dev.mygame.dto.websocket.request.PeaceProposalResultEvent;
import dev.mygame.dto.websocket.response.AbilityCooldownDto;
import dev.mygame.dto.websocket.response.EntityStateDto;
import dev.mygame.dto.websocket.response.PlayerStateDto;
import dev.mygame.enums.AbilityUseResult;
import dev.mygame.enums.EntityStateType;
import dev.mygame.mapper.EntityMapper;
import dev.mygame.service.AIService;
import dev.mygame.service.AbilityService;
import dev.mygame.service.FactionService;
import dev.mygame.service.internal.DamageResult;
import dev.mygame.enums.ActionType;
import dev.mygame.enums.CombatOutcome;
import dev.mygame.domain.event.CombatEndListener;
import dev.mygame.domain.event.GameSessionEndListener;
import dev.mygame.service.internal.EntityAction;
import lombok.Builder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static dev.mygame.config.WebSocketDestinations.PRIVATE_EVENTS_QUEUE;

@Data
@Builder
public class GameSession implements CombatEndListener {
    private String sessionID;
    private final StandartEntityGameSettings standartEntityGameSettings;

    @Builder.Default
    private Map<String, Entity> entities = new ConcurrentHashMap<>();
    private Map<String, GameObject> gameObjects;

    private GameMapHex gameMap;

    @Builder.Default
    private Map<String, CombatInstance> activeCombats = new ConcurrentHashMap<>();
    private SimpMessagingTemplate messagingTemplate;

    private final FactionService factionService;
    private final AbilityService abilityService;
    private final AIService aiService;
    private final EntityMapper entityMapper;

    // Ключ - ID приглашенного игрока, Значение - ID команды, в которую его приглашают.
    @Builder.Default
    private Map<String, String> pendingInvites = new ConcurrentHashMap<>();

    // Map<ID комбата, Map<Id юзера, его ответ на приглашение>>
    @Builder.Default
    private Map<String, Map<String, Boolean>> peacefulAgreements =  new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler;
    private static final Logger log = LoggerFactory.getLogger(GameSession.class);

    @Builder.Default
    private List<GameSessionEndListener> endListeners = new ArrayList<>();;

    public void addEntity(Entity entity) {
        if(!entities.containsKey(entity.getId())) {
            entities.put(entity.getId(), entity);
        }
    }

    public void removeEntity(Entity entity) {
        entities.remove(entity.getId());
    }

    public void publishUpdate(String updateType, Object payload) {
        Map<String, Object> action = new HashMap<>();
        action.put("actionType", updateType);
        action.put("payload", payload);

        String destination = WebSocketDestinations.GAME_UPDATES_TOPIC.replace("{sessionId}", this.sessionID);
        this.messagingTemplate.convertAndSend(destination,action);
    }

    public void handleEntityAction(String entityId, EntityAction action) {
        Entity entity = entities.get(entityId);
        System.out.println("\n--- [GameSession] handleEntityAction received ---");
        System.out.println("    From Entity ID: " + entityId + " (" + (entity != null ? entity.getName() : "NOT FOUND") + ")");
        System.out.println("    Action Type: " + action.getActionType());

        if (entity == null || entity.isDead()) {
            if (entity instanceof Player) {
                sendErrorMessageToPlayer(
                        (Player) entity,
                        "You cannot perform actions while you are dead.",
                        "PLAYER_IS_DEAD"
                );
            }
            return;
        }

        if (entity.getState() == EntityStateType.COMBAT) {
            CombatInstance combat = findCombatForEntity(entityId);

            if (combat != null) {
                System.out.println("    Entity is in Combat. Current turn is: " + combat.getCurrentTurnEntityId());
                if (!combat.getCurrentTurnEntityId().equals(entityId)) {
                    System.out.println("    !!! ACTION DENIED: Not entity's turn. !!!");
                    if (entity instanceof Player) {
                        sendErrorMessageToPlayer((Player) entity, "It's not your turn!", "NOT_YOUR_TURN");
                    }
                    return;
                }
                System.out.println("    Action PERMITTED: It is entity's turn.");
            } else {
                System.out.println("    !!! WARNING: Entity is in COMBAT state but no combat instance was found. !!!");
                return;
            }
        } else {
            System.out.println("    Entity is in EXPLORING state. Action is permitted.");
        }

        ActionType actionType = action.getActionType();
        switch (actionType) {
            case MOVE:
                entityMove(entityId, action.getTargetHex());
                break;
            case ATTACK:
                entityAttack(entityId, action.getTargetId());
                break;
//            case INTERACT:
//                entityInteract(entityId, action.getInteractId());
//                break;
//            case USE_ITEM:
//                entityUseItem(entityId, action.getItemId() , action.getPoint());
//                break;

//            case CAST_SPELL:
//                Entity caster = entities.get(entityId);
//
//                Optional<AbilityInstance> abilityId = caster.getAbilities().stream()
//                            .filter(a -> a.getTemplate().getTemplateId().equals(action.getAbilityId()))
//                            .findFirst();
//                if(abilityId.isEmpty()) {
//                    log.warn("Player {} tried to cast an unknown ability: {}", caster.getName(), action.getAbilityId());
//                    break;
//                }
//                AbilityInstance abilityInstance = abilityId.get();
//                abilityService.useAbility(this, caster, abilityInstance, action.getTargetHex());
//                break;
            case CAST_SPELL: {
                entityUseAbility(entityId, action);
                break;
            }

            case END_TURN:
                endTurn(entityId);
                break;
        }
    }

    private void entityUseAbility(String entityId, EntityAction action) {
        Entity caster = entities.get(entityId);
        if (caster == null) {
            return;
        }

        Optional<AbilityInstance> abilityInstanceOpt = caster.getAbilities().stream()
                .filter(instance -> instance.getTemplate().getTemplateId().equals(action.getAbilityId()))
                .findFirst();

        if (abilityInstanceOpt.isEmpty()) {
            return;
        }

        AbilityInstance abilityToUse = abilityInstanceOpt.get();
        Hex targetHex = action.getTargetHex();

        AbilityUseResult result = abilityService.useAbility(this, caster, abilityToUse, targetHex);

        if (result != AbilityUseResult.SUCCESS) {
            return;
        }

        // если способность дамажит, то выделем тех, кто получил дамаг, и проверяем, являются ли они противниками
        // если да, то начинаем бой, иначе - ничего
        if (!abilityToUse.getTemplate().hasDamageEffect()) {
            return;
        }

        List<Entity> affectedTargets = findTargetsInArea(targetHex, abilityToUse.getTemplate().getAreaOfEffectRadius());

        Optional<Entity> hostileTargetOpt = affectedTargets.stream()
                .filter(target -> factionService.areEnemies(caster, target))
                .findFirst();

        if (hostileTargetOpt.isEmpty()) {
            return;
        }

        Entity hostileTarget = hostileTargetOpt.get();
        // начинаем новый бой
        if (caster.getState() == EntityStateType.EXPLORING && hostileTarget.getState() == EntityStateType.EXPLORING) {
            log.info("Combat initiated by ability...");
            List<Entity> participants = findNearbyAlliesAndEnemies(caster, hostileTarget);
            startCombatWithInitiator(participants, caster);
        }
        // присоединяемся к бою
        else if (caster.getState() == EntityStateType.EXPLORING && hostileTarget.getState() == EntityStateType.COMBAT) {
            log.info("{} joins an existing combat by casting a spell!", caster.getName());
            CombatInstance combat = findCombatForEntity(hostileTarget.getId());
            if (combat != null) {
                List<Entity> joiningGroup = findNearbyAlliesAndEnemies(caster, hostileTarget);
                combat.addParticipantsToCombat(joiningGroup);
            }
        }

    }

    public void endTurn(String entityId) {
        Entity entity = entities.get(entityId);
        if (entity.getState() != EntityStateType.COMBAT) {
            log.warn("Entity {} tried to end turn while not in combat.", entity.getName());
            return;
        }

        CombatInstance combat = findCombatForEntity(entityId);
        if (combat != null) {
            log.info("Entity {} ends their turn.", entity.getName());
            combat.proceedToNextTurn();
        }
    }

    private CombatInstance findCombatForEntity(String entityId) {
        for(CombatInstance combat : activeCombats.values()) {
            boolean isInCombat = combat.getTeams().values().stream()
                    .flatMap(Set::stream)
                    .anyMatch(entity -> entity.getId().equals(entityId));
            if(isInCombat) {
                return combat;
            }
        }
        return null;
    }

    private void entityMove(String entityId, Hex targetHex) {
        Entity entity = entities.get(entityId);
        //Player player = (entity instanceof Player) ? (Player) entity : null;
        if (entity == null) {
            return;
        }
        int distance = entity.getPosition().distanceTo(targetHex);
        if (distance != 1) {
            if (entity instanceof Player)
                sendErrorMessageToPlayer((Player) entity, "You can only move to adjacent hexes.", "MOVE_INVALID_DISTANCE");
            return;
        }

        Tile targetTile = this.gameMap.getTile(targetHex);
        if (targetTile == null || !targetTile.isPassable()) {
            if (entity instanceof Player)
                sendErrorMessageToPlayer((Player) entity, "You can't move there.", "TILE_NOT_PASSABLE");
            return;
        }

        if (targetTile.isOccupied()) {
            if (entity instanceof Player)
                sendErrorMessageToPlayer((Player) entity, "This tile is occupied.", "TILE_OCCUPIED");
            return;
        }

        int moveCost = standartEntityGameSettings.getDefaultMovementCost();
        if (entity.getState() == EntityStateType.COMBAT && entity.getCurrentAP() < moveCost) {
            if (entity instanceof Player)
                sendErrorMessageToPlayer((Player) entity, "Not enough Action Points to move.", "NOT_ENOUGH_AP");
            return;
        }

        this.gameMap.getTile(entity.getPosition()).setOccupiedById(null);

        System.out.println("--- [SERVER] entityMove: " + entityId + " ---");
        System.out.println("    Position BEFORE: " + entity.getPosition());

        entity.setPosition(targetHex);
        System.out.println("    Position AFTER: " + entity.getPosition());

        this.gameMap.getTile(targetHex).setOccupiedById(entity.getId());

        if (entity.getState() == EntityStateType.COMBAT) {
            entity.setCurrentAP(entity.getCurrentAP() - moveCost);
        }

        EntityMovedEvent movedEvent = EntityMovedEvent.builder()
                .entityId(entity.getId())
                .newPosition(entity.getPosition())
                .currentAP(entity.getCurrentAP())
                .pathToAnimate(List.of(targetHex))
                .reachedTarget(true)
                .build();

        publishUpdate("entity_moved", movedEvent);
        checkForCombatStart(entity);
        if (entity.getState() == EntityStateType.COMBAT) {
            //checkAndEndTurnIfNeeded(entity);
        }
    }

    private void entityAttack(String attackerId, String targetId) {
        Entity attacker = entities.get(attackerId);
        Entity target = entities.get(targetId);

        if (target == null || target.isDead()) {
            if (attacker instanceof Player) {
                sendErrorMessageToPlayer((Player) attacker, "Target is already dead.", "TARGET_IS_DEAD");
            }
            return;
        }

        if (!isTargetInRange(attacker, target)) {
            if (attacker instanceof Player) {
                sendErrorMessageToPlayer((Player) attacker, "Target is out of range.", "TARGET_OUT_OF_RANGE");
            }
            return;
        }

        boolean isAttackerInCombat = attacker.getState() == EntityStateType.COMBAT;
        boolean isTargetInCombat = target.getState() == EntityStateType.COMBAT;

        if (!isAttackerInCombat && !isTargetInCombat) {
            if(factionService.areEnemies(attacker, target)) {
                log.info(">>> SERVER: Initiating new combat via entityAttack. Attacker: {}, Target: {}", attacker.getId(), target.getId());

                DamageResult damageResult = target.takeDamage(attacker.getAttack());
                publishAttackEvents(attacker, target, damageResult);

                List<Entity> participants = findNearbyAlliesAndEnemies(attacker, target);

                startCombatWithInitiator(participants, attacker);

            } else if(Objects.equals(attacker.getTeamId(), target.getTeamId())) {
                DamageResult damageResult = target.takeDamage(attacker.getAttack());
                publishAttackEvents(attacker, target, damageResult);
            }
            return;
        }

        if(!isAttackerInCombat && isTargetInCombat && factionService.areEnemies(attacker, target)) {
            log.info("{} joins an existing combat by attacking {}!", attacker.getName(), target.getName());
            DamageResult damageResult = target.takeDamage(attacker.getAttack());
            publishAttackEvents(attacker, target, damageResult);
            CombatInstance combat = findCombatForEntity(target.getId());
            if(combat != null) {
                combat.addParticipantsToCombat(findNearbyAlliesAndEnemies(attacker, target));
            }
        }

        if (isAttackerInCombat) {
            if (attacker.getCurrentAP() < standartEntityGameSettings.getDefaultAttackCost()) {
                if (attacker instanceof Player) {
                    sendErrorMessageToPlayer((Player) attacker, "Not enough Action Points to attack.", "NOT_ENOUGH_AP");
                }
                return;
            }
            DamageResult damageResult = target.takeDamage(attacker.getAttack());
            attacker.setCurrentAP(attacker.getCurrentAP() - standartEntityGameSettings.getDefaultAttackCost());

            publishAttackEvents(attacker, target, damageResult);
        }
    }


    private void publishAttackEvents(Entity attacker, Entity target, DamageResult damageResult) {
        EntityAttackEvent attackEvent = new EntityAttackEvent(
                attacker.getId(),
                target.getId(),
                attacker.getAttack(),
                attacker.getCurrentAP()
        );
        publishUpdate("entity_attack", attackEvent);

        EntityStatsUpdatedEvent statsUpdateEvent = EntityStatsUpdatedEvent.builder()
                .targetEntityId(target.getId())
                .damageToHp(damageResult.getDamageToHp())
                .currentHp(target.getCurrentHp())
                .absorbedByArmor(damageResult.getAbsorbedByArmor())
                .currentDefense(target.getDefense())
                .isDead(target.isDead())
                .build();
        publishUpdate("entity_stats_updated", statsUpdateEvent);
    }

    public List<Entity> findTargetsInArea(Hex centerHex, int radius) {
        if (radius == 0) {
            Entity targetInCenter = this.getEntityAt(centerHex);
            return (targetInCenter != null) ? List.of(targetInCenter) : List.of();
        }

        return this.getEntities().values().stream()
                .filter(entity -> entity.getPosition().distanceTo(centerHex) <= radius)
                .collect(Collectors.toList());
    }

    private List<Entity> findNearbyAlliesAndEnemies(Entity attacker, Entity target) {
        final int COMBAT_JOIN_RADIUS = standartEntityGameSettings.getDefaultCheckRadius();

        return entities.values().stream()
                .filter(entity -> entity.getState() == EntityStateType.EXPLORING)
                .filter(entity -> entity.getPosition().distanceTo(attacker.getPosition()) <= COMBAT_JOIN_RADIUS ||
                        entity.getPosition().distanceTo(target.getPosition()) <= COMBAT_JOIN_RADIUS)
                .filter(entity -> Objects.equals(entity.getTeamId(), attacker.getTeamId()) ||
                        Objects.equals(entity.getTeamId(), target.getTeamId()) ||
                        entity.equals(attacker) || entity.equals(target))
                .toList();
    }

    private boolean isTargetInRange(Entity attacker, Entity target) {
        if(attacker == null || target == null)
            return false;

        int attackRange = attacker.getAttackRange();

        int distance = attacker.getPosition().distanceTo(target.getPosition());
        return attackRange >= distance;
    }

    public void addGameSessionEndListener(GameSessionEndListener listener) {
        this.endListeners.add(listener);
    }

    public void removeGameSessionEndListener(GameSessionEndListener listener) {
        this.endListeners.remove(listener);
    }

    public void notifyGameSessionEndListeners() {
        for(GameSessionEndListener listener : this.endListeners) {
            listener.onGameSessionEnd(this);
        }
    }

    public Player getPlayerByWebsocketSessionId(String websocketSessionId) {
        for(Entity entity : entities.values())
            if(entity instanceof Player player)
                if(player.getWebsocketSessionId().equals(websocketSessionId))
                    return player;
        return null;
    }

    public void checkForCombatStart(Entity movedEntity) {
        if(movedEntity.getState() == EntityStateType.COMBAT)
            return;

        final int MAX_CHECK_RADIUS = 10;

        List<Entity> nearbyEntities = findAllEntitiesInRadius(movedEntity.getId(), MAX_CHECK_RADIUS);
        if (nearbyEntities.isEmpty())
            return;

        //List<Entity> nearbyEnemies = new ArrayList<>();
        boolean isEnemyPresent = false;
        String existingCombatId = null;
        for(Entity nearbyEntity : nearbyEntities)
            if(factionService.areEnemies(nearbyEntity, movedEntity)) {
                if(nearbyEntity.getState() == EntityStateType.EXPLORING) {
                    int distance = movedEntity.getPosition().distanceTo(nearbyEntity.getPosition());
                    if (distance <= movedEntity.getAggroRadius() || distance <= nearbyEntity.getAggroRadius()) {
                        // бой будет начинаться для ВСЕХ юнитов, которые находятся рядом, а не только для "врагов"
                        // потом можно создать метод для окончания боя по согласию всех участников
                        isEnemyPresent = true;
                    }
                } else {
                    CombatInstance combat = findCombatForEntity(nearbyEntity.getId());
                    if (combat != null) {
                        existingCombatId = combat.getCombatId();
                        break;
                    }
                }
            }
        if (!isEnemyPresent && existingCombatId == null) {
            return;
        }

        List<Entity> joiningGroup = new ArrayList<>(nearbyEntities.stream()
                .filter(e -> e.getState() == EntityStateType.EXPLORING)
                .toList());
        joiningGroup.add(movedEntity);

        if(existingCombatId != null) {
            log.info("{}'s group joins an existing combat!", movedEntity.getName());
            convertAbilityCooldownByEntityStateType(EntityStateType.COMBAT, joiningGroup);
            activeCombats.get(existingCombatId).addParticipantsToCombat(joiningGroup);
        } else {
            System.out.println("--- [SERVER] Combat condition met for entity: " + movedEntity.getId() + " at position " + movedEntity.getPosition() + " ---");
            startCombatByProximity(joiningGroup);
        }
    }

    private List<Entity> findAllEntitiesInRadius(String entityId, int searchingRadius) {
        Entity entity = entities.get(entityId);
        return entities.values().stream()
                .filter(e -> !e.getId().equals(entity.getId()) &&
                        entity.getPosition().distanceTo(e.getPosition()) <= searchingRadius)
                .toList();
    }

    public void startCombatWithInitiator(List<Entity> participants, Entity initiator) {
        System.out.println("--- [GameSession] Starting combat with initiator: " + initiator.getName() + " ---");
        String combatId = UUID.randomUUID().toString();
        CombatInstance combat = new CombatInstance(combatId, this, participants, aiService);

        convertAbilityCooldownByEntityStateType(EntityStateType.COMBAT, participants);

        combat.addListener(this);
        activeCombats.put(combatId, combat);
        publishCombatStartedEvent(combat, combatId);
    }

    public void startCombatByProximity(List<Entity> participants) {
        System.out.println("--- [GameSession] Starting combat by proximity ---");
        String combatId = UUID.randomUUID().toString();
        CombatInstance combat = new CombatInstance(combatId, this, participants, aiService);
        convertAbilityCooldownByEntityStateType(EntityStateType.COMBAT, participants);

        combat.addListener(this);
        activeCombats.put(combatId, combat);

        publishCombatStartedEvent(combat, combatId);
    }

    // первый аргумент - тип боя, В КОТОРЫЙ нужно перевести КД
    private void convertAbilityCooldownByEntityStateType(EntityStateType entityStateType, List<Entity> participants) {
        for(Entity participant : participants) {
            for (AbilityInstance ability : participant.getAbilities()) {
                if (entityStateType == EntityStateType.COMBAT) {
                    if (ability.getCooldownEndTime() > System.currentTimeMillis()) {
                        long timeLeftMs = ability.getCooldownEndTime() - System.currentTimeMillis();
                        long secondsLeft = timeLeftMs / 1000;
                        int turnsLeft = (int) Math.ceil((double) secondsLeft / 10.0);

                        ability.setTurnCooldown(Math.max(turnsLeft, 0));
                        ability.setCooldownEndTime(0);
                    }
                } else if (ability.getTurnCooldown() > 0) {
                    long secondsToWait = ability.getTurnCooldown() * 10L;
                    long newEndTime = System.currentTimeMillis() + secondsToWait * 1000;
                    ability.setCooldownEndTime(Math.max(newEndTime, 0));
                    ability.setTurnCooldown(0);
                }
            }
            if (participant instanceof Player) {
                publishCasterStateUpdate(participant);
            }
        }
    }
    public void publishCasterStateUpdate(Entity player) {
        List<AbilityCooldownDto> cooldowns = player.getAbilities().stream()
                .map(ab -> AbilityCooldownDto.builder()
                        .abilityTemplateId(ab.getTemplate().getTemplateId())
                        .turnCooldown(ab.getTurnCooldown())
                        .cooldownEndTime(ab.getCooldownEndTime())
                        .build())
                .collect(Collectors.toList());

        CasterStateUpdatedEvent updateEvent = CasterStateUpdatedEvent.builder()
                .casterId(player.getId())
                .newCurrentAP(player.getCurrentAP()) // AP не менялись, но поле в DTO обязательное
                .abilityCooldowns(cooldowns)
                .build();

        publishUpdate("caster_state_updated", updateEvent);
    }

    public Player getPlayerByEntityId(String entityId) {
        Entity entity =  this.entities.get(entityId);
        if(entity instanceof Player)
            return (Player) entity;
        else
            return null;
    }

    private void publishCombatStartedEvent(CombatInstance combat, String combatId) {
        List<CombatTeamDto> combatTeamDtos = combat.getTeams().entrySet().stream()
                .map(entry -> {
                    Set<String> teamMemberIds = entry.getValue().stream()
                            .map(Entity::getId)
                            .collect(Collectors.toSet());
                    return new CombatTeamDto(entry.getKey(), teamMemberIds);
                })
                .toList();

        List<EntityStateDto> combatantDtos = combat.getTurnOrder().stream()
                .map(entityId -> entities.get(entityId))
                .filter(Objects::nonNull)
                .map(entityMapper::toState)
                .collect(Collectors.toList());

        ArrayList<String> turnOrder = new ArrayList<>(combat.getTurnOrder());

        CombatStartedEvent combatStartedEvent = CombatStartedEvent.builder()
                .combatId(combatId)
                .combatInitiatorId(null)
                .teams(combatTeamDtos)
                .initialTurnOrder(turnOrder)
                .combatants(combatantDtos)
                .build();

        publishUpdate("combat_started", combatStartedEvent);
    }

    /**
     * Отправляет стандартизированное сообщение об ошибке конкретному игроку.
     * @param player Игрок, которому отправляется сообщение.
     * @param message Текстовое сообщение для пользователя.
     * @param errorCode Уникальный код ошибки для обработки на клиенте.
     */
    public void sendErrorMessageToPlayer(Player player, String message, String errorCode) {
        ErrorEvent errorEvent = new ErrorEvent(message, errorCode);

        // this.messagingTemplate должен быть доступен в этом классе
        this.messagingTemplate.convertAndSendToUser(
                player.getUserId(),
                WebSocketDestinations.ERROR_QUEUE,
                errorEvent
        );
    }

    @Override
    public void onCombatEnded(CombatInstance combat, CombatOutcome outcome, String winningTeamId, List<Entity> allParticipants) {
        log.info("Combat {} ended for team {}. Outcome: {}", combat.getCombatId(), winningTeamId, outcome);
        CombatEndedEvent event = CombatEndedEvent.builder()
                .combatId(combat.getCombatId())
                .outcome(outcome)
                .winningTeamId(winningTeamId)
                .build();

        publishUpdate("combat_ended", event);

        for (Entity participant : allParticipants) {
            if (participant.isAlive()) {
                participant.setState(EntityStateType.EXPLORING);
                participant.removeDeathListener(combat);
            }
        }

        convertAbilityCooldownByEntityStateType(EntityStateType.EXPLORING, allParticipants);
        if (outcome == CombatOutcome.VICTORY) {
            //...
        }

        long remainingTeams = combat.getTeams().values().stream()
                .filter(team -> team.stream().anyMatch(Entity::isAlive))
                .count();

        if (remainingTeams <= 1) {
            log.info("Scheduling removal of combat instance: {}", combat.getCombatId());

            scheduler.schedule(() -> {
                log.info("Executing delayed removal of combat instance: {}", combat.getCombatId());
                activeCombats.remove(combat.getCombatId());
            }, 1, TimeUnit.SECONDS); // Задержка в 1 секунду
        }
    }

    public void handleInvitationPlayerToTeam(Player inviterUser, Player targetUser) {
        if(inviterUser.equals(targetUser) && Objects.equals(inviterUser.getTeamId(), targetUser.getTeamId())) {
            sendErrorMessageToPlayer(inviterUser, "Cannot invite this player.", "INVITE_INVALID");
            return;
        }
        if(inviterUser.getTeamId() == null) {
            inviterUser.setTeamId(UUID.randomUUID().toString());
            publishTeamUpdated(inviterUser.getTeamId());
        }

        this.pendingInvites.put(targetUser.getUserId(), inviterUser.getTeamId());

        TeamInviteEvent event = new TeamInviteEvent(inviterUser.getId(), inviterUser.getName(), inviterUser.getTeamId());

        Map<String, Object> wrappedMessage = new HashMap<>();
        wrappedMessage.put("actionType", "team_invite");
        wrappedMessage.put("payload", event);

        messagingTemplate.convertAndSendToUser(
                targetUser.getUserId(),
                PRIVATE_EVENTS_QUEUE,
                wrappedMessage
        );
    }

    public void publishTeamUpdated(String teamId) {
        Set<String> membersIds = this.entities.values().stream()
                .filter(e -> Objects.equals(e.getTeamId(), teamId))
                .map(Entity::getId)
                .collect(Collectors.toSet());

        TeamUpdatedEvent event = new TeamUpdatedEvent(teamId, membersIds);
        publishUpdate("team_updated", event);
    }

    public void handleRespondPlayerToTeamInvite(Player invitedUser, boolean accepted) {
        String teamIdToJoin = pendingInvites.get(invitedUser.getUserId());

        if (teamIdToJoin == null) {
            sendErrorMessageToPlayer(invitedUser, "You have no pending invitation.", "NO_INVITE_FOUND");
            return;
        }

        pendingInvites.remove(invitedUser.getId());

        if (accepted) {
            log.info("Player {} accepted invite to team {}", invitedUser.getName(), teamIdToJoin);

            String oldTeamId = invitedUser.getTeamId();
            if (oldTeamId != null && !oldTeamId.equals(teamIdToJoin)) {
                log.info("Player {} is leaving old team {}", invitedUser.getName(), oldTeamId);
                publishTeamUpdated(oldTeamId);
            }
            invitedUser.setTeamId(teamIdToJoin);

            publishTeamUpdated(teamIdToJoin);

        } else {
            log.info("Player {} declined invite to team {}", invitedUser.getName(), teamIdToJoin);

            // мб найти пригласившего и сообщить об отказе.
        }
    }

    public Player getPlayerByUserId(String invitedUserId) {
        return entities.values().stream()
                .filter(e -> (e instanceof Player) &&
                             ((Player) e).getUserId().equals(invitedUserId))
                .map(e -> (Player) e)
                .findFirst()
                .orElse(null);
    }

    public Entity getEntityAt(Hex centerHex) {
        return entities.values().stream()
                .filter(e -> e.getPosition().equals(centerHex))
                .findFirst()
                .orElse(null);
    }

    public void handlePeaceProposal(String combatId, String initiatorUserId) {
        CombatInstance combatInstance = activeCombats.get(combatId);
        if (combatInstance == null) {
            return;
        }
        Player initiator = getPlayerByUserId(initiatorUserId);
        if (initiator == null) return;

        peacefulAgreements.put(combatId, new ConcurrentHashMap<>());
        peacefulAgreements.get(combatId).put(initiatorUserId, true);

        PeaceProposalEvent payload = new PeaceProposalEvent(initiator.getId(), initiator.getName());

        publishUpdateToCombatants(combatInstance, "peace_proposal", payload);

        handleMonsterPresentInPeace(combatId, combatInstance);
    }

    private void handleMonsterPresentInPeace(String combatId, CombatInstance combatInstance) {
        if (combatInstance.getAliveEntities().stream().anyMatch(e -> e instanceof Monster)) {
            log.warn("Peace proposal for combat {} failed because a monster is still alive.", combatId);
            peacefulAgreements.remove(combatId);

            PeaceProposalResultEvent event =
                    new PeaceProposalResultEvent(false,
                                                 "System: Monsters do not agree to peace");
            publishUpdateToCombatants(combatInstance, "peace_proposal_result", event);
        }
    }

    public void handlePeaceResponse(String combatId, String responserUserId, boolean accepted) {
        CombatInstance combatInstance = activeCombats.get(combatId);
        Map<String, Boolean> votes = peacefulAgreements.get(combatId);

        if (combatInstance == null || combatInstance.isFinished() || votes == null)
            return;

        Player responder = getPlayerByUserId(responserUserId);
        if (responder == null || !responder.isAlive())
            return;

        handleMonsterPresentInPeace(combatId, combatInstance);

        if (!accepted) {
            log.info("Peace proposal for combat {} was rejected by {}.", combatId, responder.getName());
            peacefulAgreements.remove(combatId);

            PeaceProposalResultEvent payload = new PeaceProposalResultEvent(false, responder.getName());
            publishUpdateToCombatants(combatInstance, "peace_proposal_result", payload);
            return;
        }

        votes.put(responserUserId, true);

        List<String> alivePlayerUserIds = combatInstance.getAliveEntities().stream()
                .filter(e -> e instanceof Player)
                .map(e -> ((Player) e).getUserId())
                .toList();

        boolean allPlayersVoted = alivePlayerUserIds.stream().allMatch(votes::containsKey);

        if (votes.size() >= alivePlayerUserIds.size() && allPlayersVoted) {
            log.info("Peace proposal for combat {} was unanimously accepted!", combatId);
            peacefulAgreements.remove(combatId);

            PeaceProposalResultEvent payload = new PeaceProposalResultEvent(true, null);
            publishUpdateToCombatants(combatInstance, "peace_proposal_result", payload);

            combatInstance.endCombatForAllByAgreement();
        }
    }

    private void publishUpdateToCombatants(CombatInstance combat, String updateType, Object payload) {
        List<Player> playersInCombat = combat.getAliveEntities().stream()
                .filter(e -> e instanceof Player)
                .map(e -> (Player) e)
                .toList();

        Map<String, Object> updateMessage = new HashMap<>();
        updateMessage.put("actionType", updateType);
        updateMessage.put("payload", payload);

        for (Player player : playersInCombat) {
            messagingTemplate.convertAndSendToUser(
                    player.getUserId(),
                    WebSocketDestinations.PRIVATE_EVENTS_QUEUE,
                    updateMessage
            );
        }
    }

    private List<Entity> getTeammatesByEntityId(Entity entity) {
        return this.entities.values().stream()
                .filter(e -> e.getTeamId().equals(entity.getTeamId()))
                .toList();
    }

    public void handleLeaveFromTeam(String userId) {
        Player playerWhoLeaves = getPlayerByUserId(userId);
        if(playerWhoLeaves == null)
            return;

        String oldTeamId = playerWhoLeaves.getTeamId();

        String newSoloTeamId = UUID.randomUUID().toString();
        playerWhoLeaves.setTeamId(newSoloTeamId);

        publishTeamUpdated(oldTeamId);
        publishTeamUpdated(playerWhoLeaves.getTeamId());
    }
}
