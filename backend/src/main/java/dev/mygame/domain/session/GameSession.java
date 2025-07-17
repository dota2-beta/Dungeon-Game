package dev.mygame.domain.session;

//import dev.mygame.game.model.map.Map;
import dev.mygame.config.GameSettings;
import dev.mygame.config.WebSocketDestinations;
import dev.mygame.domain.model.Entity;
import dev.mygame.domain.model.GameObject;
import dev.mygame.domain.model.Player;
import dev.mygame.domain.model.map.*;
import dev.mygame.dto.websocket.response.event.*;
import dev.mygame.enums.EntityStateType;
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
import java.util.stream.Collectors;

@Data
@Builder
public class GameSession implements CombatEndListener {
    private String sessionID;
    private final GameSettings gameSettings;

    private Map<String, Entity> entities;
    private Map<String, GameObject> gameObjects;

    private GameMapHex gameMap;
    private Map<String, CombatInstance> activeCombats = new HashMap<>();
    private SimpMessagingTemplate messagingTemplate;

    private final FactionService factionService;

    private static final Logger log = LoggerFactory.getLogger(GameSession.class);

    @Builder.Default
    private List<GameSessionEndListener> endListeners = new ArrayList<>();;

    public void addEntity(Entity entity) {
//        if(entity instanceof Player) {
//            entities.put(((Player) entity).getUserId(), entity);
//        }
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
                if (!combat.getCurrentTurnEntityId().equals(entityId)) {
                    log.warn("Player {} tried to perform action out of turn.", entity.getName());
                    if (entity instanceof Player) {
                        sendErrorMessageToPlayer((Player) entity, "It's not your turn!", "NOT_YOUR_TURN");
                    }
                    return;
                }
            } else {
                // сущность в состоянии COMBAT, но не найдена ни в одном бою, не должно быть
                log.error("Entity {} is in COMBAT state but not found in any active combat instance.", entity.getName());
                return;
            }
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
//            case END_TURN:
//                endTurn();
//                break;
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

        if (!(entity instanceof Player)) {
            // если это не игрок, то мы не можем отправить ему личное сообщение
            // Просто выходим или обрабатываем по-другоgому
            return;
        }
        Player player = (Player) entity;

        Tile targetTile = this.gameMap.getTile(targetHex);

        if (player.getPosition().equals(targetHex)) {
            sendErrorMessageToPlayer(player, "You are already here.", "ALREADY_AT_TARGET");
            return;
        }

        if (targetTile == null || !targetTile.isPassable()) {
            sendErrorMessageToPlayer(player, "You can't move there.", "TILE_NOT_PASSABLE");
            return;
        }

        if (targetTile.isOccupied()) {
            sendErrorMessageToPlayer(player, "This tile is occupied.", "TILE_OCCUPIED");
            return;
        }

        //Tile targetTile = this.gameMap.getTile(targetHex);
        List<Hex> fullPath = this.gameMap.findPath(player.getPosition(), targetHex);

        if (fullPath == null || fullPath.isEmpty()) {
            sendErrorMessageToPlayer(player, "No path found to the target.", "NO_PATH_FOUND");
            return;
        }

        List<Hex> reachablePath = new ArrayList<>();
        Hex reachableHex = player.getPosition();
        int totalCost = 0;

        if(player.getState() == EntityStateType.COMBAT) {
            // логика для AP в бою
            for (int i = 1; i < fullPath.size(); i++) {
                if (totalCost + gameSettings.getDefaultMovementCost() <= player.getCurrentAP()) {
                    totalCost += gameSettings.getDefaultMovementCost();
                    reachableHex = fullPath.get(i);
                    reachablePath.add(reachableHex);
                } else {
                    break;
                }
            }

            if (reachablePath.isEmpty()) {
                sendErrorMessageToPlayer(player, "Not enough Action Points to move.", "NOT_ENOUGH_AP");
                return;
            }
        } else {
            reachablePath = fullPath.subList(1, fullPath.size());
            reachableHex = fullPath.get(fullPath.size() - 1);
        }

        this.gameMap.getTile(player.getPosition()).setOccupiedById(null);

        player.setPosition(reachableHex);
        player.setCurrentAP(player.getCurrentAP() - totalCost);

        this.gameMap.getTile(reachableHex).setOccupiedById(player.getId());
        EntityMovedEvent movedEvent = new EntityMovedEvent(
                player.getId(),
                player.getPosition(),
                player.getCurrentAP(),
                reachablePath,
                reachableHex.equals(targetHex)
        );

        publishUpdate("entity_moved", movedEvent);

        checkForCombatStart(player);
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
                log.info("{} initiates combat by attacking {}!", attacker.getName(), target.getName());

                // наносим бесплатный урон
                DamageResult damageResult = target.takeDamage(attacker.getAttack());
                publishAttackEvents(attacker, target, damageResult); // Выносим отправку событий в отдельный метод

                // Собираем участников и начинаем бой
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
            if (attacker.getCurrentAP() < gameSettings.getDefaultAttackCost()) {
                if (attacker instanceof Player) {
                    sendErrorMessageToPlayer((Player) attacker, "Not enough Action Points to attack.", "NOT_ENOUGH_AP");
                }
                return;
            }
            DamageResult damageResult = target.takeDamage(attacker.getAttack());
            attacker.setCurrentAP(attacker.getCurrentAP() - gameSettings.getDefaultAttackCost());

            publishAttackEvents(attacker, target, damageResult);
        }

        // Если бой был инициирован этой атакой, то ход атакующего на этом, по сути, заканчивается.
        // У него будет полный набор AP на его первый официальный ход в бою.
        // Дальнейшие действия (например, endTurn) будет делать клиент.
    }

    /**
     * Вспомогательный метод, чтобы не дублировать код отправки событий.
     */
    private void publishAttackEvents(Entity attacker, Entity target, DamageResult damageResult) {
        EntityAttackEvent attackEvent = new EntityAttackEvent(
                attacker.getId(),
                target.getId(),
                attacker.getAttack()
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


    private List<Entity> findNearbyAlliesAndEnemies(Entity attacker, Entity target) {
        final int COMBAT_JOIN_RADIUS = 10;

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

    private void checkForCombatStart(Entity movedEntity) {
        if(movedEntity.getState() == EntityStateType.COMBAT)
            return;

        final int MAX_CHECK_RADIUS = 10;

        List<Entity> nearbyEntities = findAllEntitiesInRadius(movedEntity.getId(), MAX_CHECK_RADIUS);
        if (nearbyEntities.isEmpty())
            return;

        List<Entity> nearbyAllies = new ArrayList<>();
        if(movedEntity.getTeamId() != null)
            nearbyAllies = nearbyEntities.stream()
                    .filter(e -> Objects.equals(e.getTeamId(), movedEntity.getTeamId()))
                    .toList();

        List<Entity> nearbyEnemies = new ArrayList<>();

        String existingCombatId = null;
        for(Entity nearbyEntity : nearbyEntities)
            if(factionService.areEnemies(nearbyEntity, movedEntity)) {
                if(nearbyEntity.getState() == EntityStateType.EXPLORING) {
                    int distance = movedEntity.getPosition().distanceTo(nearbyEntity.getPosition());
                    if (distance <= movedEntity.getAggroRadius() || distance <= nearbyEntity.getAggroRadius())
                        nearbyEnemies.add(nearbyEntity);
                } else {
                    CombatInstance combat = findCombatForEntity(nearbyEntity.getId());
                    if (combat != null) {
                        existingCombatId = combat.getCombatId();
                    }
                }
            }
        if (nearbyEnemies.isEmpty() && existingCombatId == null) {
            return;
        }

        List<Entity> joiningGroup = new ArrayList<>();
        joiningGroup.add(movedEntity);
        joiningGroup.addAll(nearbyAllies);

        if(existingCombatId != null) {
            log.info("{}'s group joins an existing combat!", movedEntity.getName());
            activeCombats.get(existingCombatId).addParticipantsToCombat(joiningGroup);
        } else {
            log.info("Combat triggered by proximity! Participants: {}", nearbyEnemies.stream().map(Entity::getName).collect(Collectors.toList()));
            joiningGroup.addAll(nearbyEnemies);
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
        String combatId = UUID.randomUUID().toString();
        CombatInstance combat = new CombatInstance(combatId, this, participants, initiator);

        combat.addListener(this);
        activeCombats.put(combatId, combat);

        publishCombatStartedEvent(combat, combatId);
    }

    public void startCombatByProximity(List<Entity> participants) {
        String combatId = UUID.randomUUID().toString();
        CombatInstance combat = new CombatInstance(combatId, this, participants);

        combat.addListener(this);
        activeCombats.put(combatId, combat);

        publishCombatStartedEvent(combat, combatId);
    }

    private void publishCombatStartedEvent(CombatInstance combat, String combatId) {
        List<CombatTeamDto> combatTeamDtos = combat.getTeams().entrySet().stream()
                .map(entry -> {
                    Set<String> teamIds = entry.getValue().stream()
                            .map(Entity::getId)
                            .collect(Collectors.toSet());
                    return new CombatTeamDto(entry.getKey(), teamIds);
                })
                .toList();

        CombatStartedEvent combatStartedEvent = CombatStartedEvent.builder()
                .combatId(combatId)
                .combatInitiatorId(null)
                .teams(combatTeamDtos)
                .initialTurnOrder(new ArrayList<>(combat.getTurnOrder()))
                .build();

        publishUpdate("combat_started", combatStartedEvent);
    }

    /**
     * Отправляет стандартизированное сообщение об ошибке конкретному игроку.
     * @param player Игрок, которому отправляется сообщение.
     * @param message Текстовое сообщение для пользователя.
     * @param errorCode Уникальный код ошибки для обработки на клиенте.
     */
    private void sendErrorMessageToPlayer(Player player, String message, String errorCode) {
        ErrorEvent errorEvent = new ErrorEvent(message, errorCode);

        // this.messagingTemplate должен быть доступен в этом классе
        this.messagingTemplate.convertAndSendToUser(
                player.getUserId(),
                WebSocketDestinations.ERROR_QUEUE, // Ваша константа для очереди ошибок
                errorEvent
        );
    }

    @Override
    public void onCombatEnded(CombatInstance combat, CombatOutcome outcome, String forTeamId) {
        if (outcome == CombatOutcome.VICTORY) {
        }

        long remainingTeams = combat.getTeams().values().stream()
                .filter(team -> team.stream().anyMatch(Entity::isAlive))
                .count();

        if (remainingTeams <= 1) {
            System.out.println("Only one team remains. Removing combat instance: " + combat.getCombatId());
            // Если осталась одна или ноль команд, бой для всех окончен.
            activeCombats.remove(combat.getCombatId());
        }
    }
}
