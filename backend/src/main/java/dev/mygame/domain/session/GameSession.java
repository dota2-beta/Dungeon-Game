package dev.mygame.domain.session;

//import dev.mygame.game.model.map.Map;
import dev.mygame.config.GameSettings;
import dev.mygame.config.WebSocketDestinations;
import dev.mygame.domain.model.Entity;
import dev.mygame.domain.model.GameObject;
import dev.mygame.domain.model.Player;
import dev.mygame.dto.websocket.response.event.*;
import dev.mygame.service.internal.DamageResult;
import dev.mygame.enums.ActionType;
import dev.mygame.enums.CombatOutcome;
import dev.mygame.domain.event.CombatEndListener;
import dev.mygame.domain.model.map.GameMap;
import dev.mygame.domain.model.map.Point;
import dev.mygame.domain.event.GameSessionEndListener;
import dev.mygame.service.internal.EntityAction;
import lombok.Builder;
import lombok.Data;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.*;

@Data
@Builder
public class GameSession implements CombatEndListener {
    private String sessionID;
    private final GameSettings gameSettings;

    private Map<String, Entity> entities;
    private Map<String, GameObject> gameObjects;

    private GameMap gameMap;
    private List<CombatInstance> activeCombats;
    private SimpMessagingTemplate messagingTemplate;

    @Builder.Default
    private List<GameSessionEndListener> endListeners = new ArrayList<>();;

//    public GameSession(String sessionID, GameSettings gameSettings, GameMap gameMap, SimpMessagingTemplate messagingTemplate) {
//        this.sessionID = sessionID;
//        this.gameSettings = gameSettings;
//        this.entities = new HashMap<>();
//        this.gameObjects = new HashMap<>();
//        this.gameMap = gameMap;
//        this.activeCombats = new ArrayList<>();
//        this.messagingTemplate = messagingTemplate;
//    }

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

        ActionType actionType = action.getActionType();
        switch (actionType) {
            case MOVE:
                entityMove(entityId, action.getTargetPoint());
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

    private void entityMove(String entityId, Point targetPoint) {
        Entity entity = entities.get(entityId);

        if (!(entity instanceof Player)) {
            // если это не игрок, то мы не можем отправить ему личное сообщение
            // Просто выходим или обрабатываем по-другоgому
            return;
        }
        Player player = (Player) entity;

//        if(player.getPosition().equals(targetPoint)) {
//            String message = "It's pointless.";
//            this.messagingTemplate.convertAndSendToUser(
//                    player.getUserId(),
//                    WebSocketDestinations.ERROR_QUEUE,
//                    Map.of("error", message)
//            );
//            return;
//        }
//
//        if(!this.gameMap.isWithinBounds(targetPoint) || !this.gameMap.getTile(targetPoint).isPassable()) {
//            String message = "You can't get through here";
//            this.messagingTemplate.convertAndSendToUser(
//                    player.getUserId(),
//                    WebSocketDestinations.ERROR_QUEUE,
//                    Map.of("error", message)
//            );
//            return;
//        }
//
//        List<Point> fullPath = this.gameMap.findPath(player.getPosition(), targetPoint);
//
//        if(fullPath == null || fullPath.isEmpty()) {
//            String message = "No path found to the target.";
//            this.messagingTemplate.convertAndSendToUser(
//                    player.getUserId(),
//                    WebSocketDestinations.ERROR_QUEUE,
//                    Map.of("error", message)
//            );
//            return;
//        }
        if (player.getPosition().equals(targetPoint)) {
            sendErrorMessageToPlayer(player, "You are already here.", "ALREADY_AT_TARGET");
            return;
        }

        if (!this.gameMap.isWithinBounds(targetPoint) || !this.gameMap.getTile(targetPoint).isPassable()) {
            sendErrorMessageToPlayer(player, "You can't move there.", "TILE_NOT_PASSABLE");
            return;
        }

        List<Point> fullPath = this.gameMap.findPath(player.getPosition(), targetPoint);

        if (fullPath == null || fullPath.isEmpty()) {
            sendErrorMessageToPlayer(player, "No path found to the target.", "NO_PATH_FOUND");
            return;
        }

        Point reachablePoint = fullPath.get(fullPath.size() - 1);

        // логика для AP в бою
//        Point reachablePoint = player.getPosition();
//        List<Point> reachablePath = new ArrayList<>();
//        int reachableCost = 0;
//        for(int i = 1; i < fullPath.size(); i++) {
//            Point currentPoint = fullPath.get(i);
//            Point previousPoint = fullPath.get(i - 1);
//
//            int stepCost = GameMap.calculateStepCost(currentPoint, previousPoint);
//            if(reachableCost + stepCost <= player.getCurrentAP()) {
//                reachableCost += stepCost;
//                reachablePoint = currentPoint;
//                reachablePath.add(reachablePoint);
//            } else {
//                break;
//            }
//        }

//        if(reachablePath.isEmpty()) {
//            String destination = "/user/" + player.getId() + "/queue/errors";
//            String message = "Not enough Action Points to move.";
//            this.messagingTemplate.convertAndSend(destination, message);
//            return;
//        }

        this.gameMap.getTile(player.getPosition()).setOccupiedById(null);
        player.setPosition(reachablePoint);
        // для боя
        // player.setCurrentAP(player.getCurrentAP() - reachableCost);
        this.gameMap.getTile(reachablePoint).setOccupiedById(player.getId());
        EntityMovedEvent movedEvent = new EntityMovedEvent(
                player.getId(),
                player.getPosition(),
                player.getCurrentAP(),
                fullPath,
                reachablePoint.equals(targetPoint)
        );

        publishUpdate("entity_moved", movedEvent);
    }

    private void entityAttack(String entityId, String targetId) {
        Entity entity = entities.get(entityId);
        Entity target = entities.get(targetId);
        if(target == null || target.getCurrentHp() <= 0) {
            if (entity instanceof Player) {
                sendErrorMessageToPlayer(
                        (Player) entity,
                        "Target is already dead.",
                        "TARGET_IS_DEAD"
                );
            }
            return;
        }

        if(!isTargetInRange(entity, target)) {
            if (entity instanceof Player) {
                sendErrorMessageToPlayer(
                        (Player) entity,
                        "Target is out of range.",
                        "TARGET_OUT_OF_RANGE"
                );
            }
            return;
        }

        // логика для боя
//        if(entity.getCurrentAP() < gameSettings.getDefaultAttackCost())
//        {
//            String destination = "/user/" + entity.getId() + "/queue/errors";
//            String message = "Not enough Action Points to attack.";
//            this.messagingTemplate.convertAndSend(destination, message);
//            return;
//        }

//        if(entity == null || !entity.isAlive()
//           || target == null || !target.isAlive()) {
//            return;
//        }

        DamageResult damageResult = target.takeDamage(entity.getAttack());
        //entity.setCurrentAP(entity.getCurrentAP() - gameSettings.getDefaultAttackCost());

        EntityAttackEvent attackEvent = new EntityAttackEvent(
                entityId,
                targetId,
                entity.getAttack()
        );
        // 2. Отправляем типизированный объект
        publishUpdate("entity_attack", attackEvent);

        EntityStatsUpdatedEvent statsUpdateEvent = new EntityStatsUpdatedEvent(
                targetId,
                damageResult.getAbsorbedByArmor(),
                damageResult.getDamageToHp(),
                target.getCurrentHp(),
                target.getDefense(),
                target.isDead()
        );
// 2. Отправляем типизированный объект
        System.out.println("======================================================");
        System.out.println("PREPARING TO SEND 'entity_stats_updated' EVENT");
        System.out.println("Target ID: " + statsUpdateEvent.getTargetEntityId());
        System.out.println("HP: " + statsUpdateEvent.getCurrentHp());
        System.out.println("isDead from DTO: " + statsUpdateEvent.isDead());
        System.out.println("======================================================");

        publishUpdate("entity_stats_updated", statsUpdateEvent);
    }

    private boolean isTargetInRange(Entity attacker, Entity target) {
        if(attacker == null || target == null)
            return false;

        int attackRange = attacker.getAttackRange();

        int distance = this.gameMap.getDistance(attacker.getPosition(), target.getPosition());

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

    private void combatStart(List<String> entityIds, String combatInitiatorId) {
        String combatId = UUID.randomUUID().toString();
        List<Entity> combatEntities = takeEntitiesById(entityIds);
        CombatInstance combatInstance = new CombatInstance(combatId, this, combatEntities, combatInitiatorId);
        activeCombats.add(combatInstance);
        combatInstance.addListener(this);

        // переделать на Set
        CombatStartedEvent combatStartedEvent = new CombatStartedEvent(
                combatId,
                combatInitiatorId,
                combatInstance.getTeam1(),
                combatInstance.getTeam2()
        );

        publishUpdate("combat_started", combatStartedEvent);
    }

    private List<Entity> takeEntitiesById(List<String> entityIds) {
        List<Entity> entityList = new ArrayList<>();
        for(String entityId : entityIds)
            entityList.add(entities.get(entityId));
        return entityList;
    }

    private CombatInstance findCombatInstanceById(int combatId) {
        return activeCombats.get(combatId);
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
    public void onCombatEnded(CombatInstance combatInstance, CombatOutcome combatOutcome) {
        combatInstance.removeListener(this);
        boolean removed = this.activeCombats.removeIf(combat -> combat == combatInstance);
        System.out.println("Combat " + combatInstance.getCombatId() + " ended with outcome: " + combatOutcome);



        CombatEndedEvent combatEndedEvent = new CombatEndedEvent(
                combatInstance.getCombatId(),
                combatOutcome
        );

        this.publishUpdate("combat_ended", combatEndedEvent);
    }
}
