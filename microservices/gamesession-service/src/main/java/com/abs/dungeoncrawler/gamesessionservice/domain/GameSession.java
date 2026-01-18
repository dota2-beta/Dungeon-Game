package com.abs.dungeoncrawler.gamesessionservice.domain;

import com.abs.dungeoncrawler.gamesessionservice.domain.model.Entity;
import com.abs.dungeoncrawler.gamesessionservice.domain.model.Monster;
import com.abs.dungeoncrawler.gamesessionservice.domain.model.Player;
import com.abs.dungeoncrawler.gamesessionservice.domain.model.map.GameMapHex;
import lombok.Data;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class GameSession {
    private String sessionId;
    private Map<String, Entity> entities = new ConcurrentHashMap<>();
    private GameMapHex gameMap;
    private Map<String, Combat> activeCombats = new ConcurrentHashMap<>();

    public GameSession(String sessionId) {
        this.sessionId = sessionId;
    }

    public void addEntity(Entity entity) {
        entities.put(entity.getId(), entity);
    }
    public Optional<Player> getPlayerByUserId(String userId) {
        return entities.values().stream()
                .filter(entity -> {
                    if(entity instanceof Player player) {
                        return player.getUserId().equals(userId);
                    }
                    return false;
                })
                .map(entity -> (Player) entity)
                .findFirst();
    }

    public Entity getEntityById(String entityId) {
        return entities.get(entityId);
    }

    public void addCombat(Combat combat) {
        activeCombats.put(combat.getId(), combat);
    }

    public Combat getCombat(String combatId) {
        return activeCombats.get(combatId);
    }

    public Combat removeCombat(String combatId) {
        return activeCombats.remove(combatId);
    }

    public Optional<Combat> findCombatByEntityId(String entityId) {
        for(Combat combat : activeCombats.values()) {
            boolean isInCombat = combat.getParticipantIds().contains(entityId);
            if(isInCombat) {
                return Optional.of(combat);
            }
        }
        return Optional.empty();
    }
}