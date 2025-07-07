package dev.mygame.game.session;

//import dev.mygame.game.model.map.Map;
import dev.mygame.config.GameSettings;
import dev.mygame.config.WebSocketDestinations;
import dev.mygame.dto.DamageResult;
import dev.mygame.game.enums.ActionType;
import dev.mygame.game.enums.CombatOutcome;
import dev.mygame.game.session.event.CombatEndListener;
import dev.mygame.game.model.*;
import dev.mygame.game.model.map.GameMap;
import dev.mygame.game.model.map.Point;
import dev.mygame.game.session.event.GameSessionEndListener;
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
                String message = "You cannot perform actions while you are dead.";
                this.messagingTemplate.convertAndSendToUser(
                        ((Player) entity).getUserId(),
                        WebSocketDestinations.ERROR_QUEUE,
                        Map.of("error", message)
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

        if(player.getPosition().equals(targetPoint)) {
            String message = "It's pointless.";
            this.messagingTemplate.convertAndSendToUser(
                    player.getUserId(),
                    WebSocketDestinations.ERROR_QUEUE,
                    Map.of("error", message)
            );
            return;
        }

        if(!this.gameMap.isWithinBounds(targetPoint) || !this.gameMap.getTile(targetPoint).isPassable()) {
            String message = "You can't get through here";
            this.messagingTemplate.convertAndSendToUser(
                    player.getUserId(),
                    WebSocketDestinations.ERROR_QUEUE,
                    Map.of("error", message)
            );
            return;
        }

        List<Point> fullPath = this.gameMap.findPath(player.getPosition(), targetPoint);

        if(fullPath == null || fullPath.isEmpty()) {
            String message = "No path found to the target.";
            this.messagingTemplate.convertAndSendToUser(
                    player.getUserId(),
                    WebSocketDestinations.ERROR_QUEUE,
                    Map.of("error", message)
            );
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

        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put("entityId", player.getId());
        updatePayload.put("newPosition", player.getPosition());
        updatePayload.put("remainingAP", player.getCurrentAP());
        updatePayload.put("pathToAnimate", fullPath);
        // updatePayload.put("pathToAnimate", reachablePath);
        updatePayload.put("reachedTarget", reachablePoint.equals(targetPoint));

        publishUpdate("entity_moved", updatePayload);
    }

    private void entityAttack(String entityId, String targetId) {
        Entity entity = entities.get(entityId);
        Entity target = entities.get(targetId);
        if(target == null) {
            String message = "Target is dead.";
            this.messagingTemplate.convertAndSendToUser(
                    ((Player) entity).getUserId(),
                    WebSocketDestinations.ERROR_QUEUE,
                    Map.of("error", message)
            );
            return;
        }

        if(!isTargetInRange(entity, target) &&  entity instanceof Player) {
            String message = "Target is out of range.";
            this.messagingTemplate.convertAndSendToUser(
                    ((Player) entity).getUserId(),
                    WebSocketDestinations.ERROR_QUEUE,
                    Map.of("error", message)
            );
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

        Map<String, Object> attackMessage = new HashMap<>();
        attackMessage.put("attackerEntityId", entityId);
        attackMessage.put("targetEntityId", targetId);
        attackMessage.put("damageCaused", entity.getAttack());
        publishUpdate("entity_attack", attackMessage);

        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put("targetEntityId", targetId);
        updatePayload.put("absorbedByArmor", damageResult.getAbsorbedByArmor());
        updatePayload.put("damageToHp", damageResult.getDamageToHp());
        updatePayload.put("currentHp", target.getCurrentHp());
        updatePayload.put("currentDefense", target.getDefense());
        updatePayload.put("isDead", target.isDead());
        publishUpdate("entity_stats_updated", updatePayload);
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

        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put("combatId", combatId);
        updatePayload.put("combatParticipants", combatEntities);
        updatePayload.put("combatInitiatorId", combatInitiatorId);
        updatePayload.put("combatTeam1", combatInstance.getTeam1());
        updatePayload.put("combatTeam2", combatInstance.getTeam2());
        publishUpdate("combat_started", updatePayload);
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

    @Override
    public void onCombatEnded(CombatInstance combatInstance, CombatOutcome combatOutcome) {
        combatInstance.removeListener(this);
        boolean removed = this.activeCombats.removeIf(combat -> combat == combatInstance);
        System.out.println("Combat " + combatInstance.getCombatId() + " ended with outcome: " + combatOutcome);



        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put("combatId", combatInstance.getCombatId());
        updatePayload.put("combatStatus", combatOutcome);
        this.publishUpdate("combatEnd", updatePayload);
    }
}
