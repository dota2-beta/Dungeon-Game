package com.abs.dungeoncrawler.gamesessionservice.services;

import com.abs.dungeonCrawler.eventcontracts.Dto.HexDto;
import com.abs.dungeonCrawler.eventcontracts.eventDto.EntityAttackEvent;
import com.abs.dungeonCrawler.eventcontracts.eventDto.EntityDiedEvent;
import com.abs.dungeonCrawler.eventcontracts.eventDto.EntityMovedEvent;
import com.abs.dungeonCrawler.eventcontracts.eventDto.EntityStatsUpdatedEvent;
import com.abs.dungeoncrawler.gamesessionservice.clients.GameDataClient;
import com.abs.dungeoncrawler.gamesessionservice.config.StandartEntityGameSettings;
import com.abs.dungeoncrawler.gamesessionservice.domain.Combat;
import com.abs.dungeoncrawler.gamesessionservice.domain.DamageResult;
import com.abs.dungeoncrawler.gamesessionservice.domain.GameSession;
import com.abs.dungeoncrawler.gamesessionservice.domain.model.Entity;
import com.abs.dungeonCrawler.eventcontracts.enums.EntityState;
import com.abs.dungeoncrawler.gamesessionservice.domain.model.Player;
import com.abs.dungeoncrawler.gamesessionservice.domain.model.map.GameMapHex;
import com.abs.dungeoncrawler.gamesessionservice.domain.model.map.Hex;
import com.abs.dungeoncrawler.gamesessionservice.domain.model.map.Tile;
import com.abs.dungeoncrawler.gamesessionservice.exception.GameActionException;
import com.abs.dungeoncrawler.gamesessionservice.kafka.KafkaEventPublisher;
import com.abs.dungeoncrawler.gamesessionservice.mapper.GameMapMapper;
import com.dungeoncrawler.contracts.grpc.gamedata.GameMapResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Data
@Service
@RequiredArgsConstructor
@Slf4j
public class CoreGameEngine {
    private final GameDataClient gameDataClient;
    private final GameMapMapper gameMapMapper;
    private final MonsterSpawner spawner;
    private final KafkaEventPublisher publisher;
    private final FactionService factionService;
    private final StandartEntityGameSettings standartEntityGameSettings;
    private final CombatService combatService;

    public GameSession createSession(String sessionId) {
        GameSession gameSession = new GameSession(sessionId);
        GameMapResponse gameMapResponse = gameDataClient.getGameMap("dungeon_level_1.txt")
                .orElseThrow(() -> new IllegalArgumentException("No game map found"));
        gameSession.setGameMap(gameMapMapper.grpcGameMapToGameMapHex(gameMapResponse));
        spawner.spawnMonsters(gameSession);
        return gameSession;
    }
    public void handleMoveAction(GameSession gameSession, String entityId, Hex targetHex) throws Exception {
        GameMapHex gameMap = gameSession.getGameMap();
        Entity entity = gameSession.getEntityById(entityId);
        if (entity == null) {
            throw new Exception("Entity not found");
        }
        if (entity.isDead()) {
            throw new GameActionException("You are dead and cannot move.", "ENTITY_IS_DEAD");
        }
        validateTurn(gameSession, entity);
        int distance = entity.getPosition().distanceTo(targetHex);
        if (distance != 1) {
            if (entity instanceof Player)
                throw new GameActionException("Too far", "MOVE_INVALID_DISTANCE");
            return;
        }

        Tile targetTile = gameMap.getTile(targetHex);
        if (targetTile == null || !targetTile.isPassable()) {
            if (entity instanceof Player)
                throw new GameActionException("Wall here", "TILE_NOT_PASSABLE");
            return;
        }

        if (targetTile.isOccupied()) {
            if (entity instanceof Player)
                throw new GameActionException("Tile is occupied", "TILE_OCCUPIED");
            return;
        }

        int moveCost = standartEntityGameSettings.getDefaultMovementCost();
        if (entity.getState() == EntityState.COMBAT && entity.getCurrentAP() < moveCost) {
            if (entity instanceof Player)
                throw new GameActionException("Not enough Action Points to move", "NOT_ENOUGH_AP");
            return;
        }
        gameMap.getTile(entity.getPosition()).setOccupiedById(null);
        entity.setPosition(targetHex);
        gameMap.getTile(targetHex).setOccupiedById(entity.getId());

        if (entity.getState() == EntityState.COMBAT) {
            entity.setCurrentAP(entity.getCurrentAP() - moveCost);
        }

        EntityMovedEvent event = EntityMovedEvent.builder()
                .sessionId(gameSession.getSessionId())
                .entityId(entityId)
                .newPosition(HexDto.builder()
                        .q(targetHex.getQ())
                        .r(targetHex.getR())
                        .build())
                .currentAP(entity.getCurrentAP())
                .build();

        publisher.publishEntityMovedEvent(event);
        if (entity.getState() == EntityState.EXPLORING) {
            combatService.checkForCombatStart(gameSession, entity);
        }
        else if (entity.getState() == EntityState.COMBAT) {
            combatService.checkAggroDuringCombat(gameSession, entity);
        }
    }

    public void handleAttackAction(GameSession gameSession, String attackerId, String targetId) throws Exception {
        Entity attacker = gameSession.getEntityById(attackerId);
        Entity target = gameSession.getEntityById(targetId);

        if (attacker == null) {
            throw new Exception("Entity not found");
        }
        if (attacker.isDead()) {
            throw new GameActionException("You are dead and cannot attack.", "ENTITY_IS_DEAD");
        }
        validateTurn(gameSession, attacker);

        if (target == null || target.isDead()) {
            if (attacker instanceof Player) {
                throw new GameActionException("Target is already dead", "TARGET_IS_DEAD");
            }
            return;
        }

        if (!isTargetInRange(attacker, target)) {
            if (attacker instanceof Player) {
                throw new GameActionException("Target is out of range", "TARGET_OUT_OF_RANGE");
            }
            return;
        }

        boolean isAttackerInCombat = attacker.getState() == EntityState.COMBAT;
        boolean isTargetInCombat = target.getState() == EntityState.COMBAT;
        boolean areEnemies = factionService.areEnemies(attacker, target);

        if (!isAttackerInCombat) {
            if (!isTargetInCombat) {
                if (areEnemies) {
                    log.info("Initiating new combat. Attacker: {}, Target: {}", attacker.getId(), target.getId());
                    applyDamage(gameSession, attacker, target); // Сначала урон!

                    List<Entity> participants = combatService.findNearbyAlliesAndEnemies(gameSession, attacker, target);
                    combatService.startCombat(gameSession, participants);
                } else {
                    applyDamage(gameSession, attacker, target);
                }
                return;
            }

            if (isTargetInCombat && areEnemies) {
                log.info("{} joins existing combat via attack!", attacker.getName());
                applyDamage(gameSession, attacker, target);

                Optional<Combat> combat = gameSession.findCombatByEntityId(target.getId());
                combat.ifPresent(value ->
                        combatService.addParticipantsToCombat(
                                gameSession,
                                value.getId(),
                                combatService.findNearbyAlliesAndEnemies(gameSession, attacker, target)
                        )
                );
                return;
            }
        }

        if (isAttackerInCombat) {
            if (attacker.getCurrentAP() < standartEntityGameSettings.getDefaultAttackCost()) {
                if (attacker instanceof Player) {
                    throw new GameActionException("Not enough Action Points to attack", "NOT_ENOUGH_AP");
                }
                return;
            }

            applyDamage(gameSession, attacker, target);

            if (isTargetInCombat) {
                Optional<Combat> combatA = gameSession.findCombatByEntityId(attacker.getId());
                Optional<Combat> combatB = gameSession.findCombatByEntityId(target.getId());

                if (combatA.isPresent() && combatB.isPresent()) {
                    Combat c1 = combatA.get();
                    Combat c2 = combatB.get();

                    if (!c1.getId().equals(c2.getId())) {
                        log.info("Merging combat {} into {}", c2.getId(), c1.getId());
                        combatService.mergeCombats(gameSession, c1, c2);
                    }
                }
            } else {
                combatService.checkAggroDuringCombat(gameSession, attacker);
            }
        }
    }

    private void applyDamage(GameSession session, Entity attacker, Entity target) {
        int damage = attacker.getAttack();
        DamageResult result = target.takeDamage(damage);
        attacker.setCurrentAP(attacker.getCurrentAP() - standartEntityGameSettings.getDefaultAttackCost());
        EntityAttackEvent attackEvent = EntityAttackEvent.builder()
                .sessionId(session.getSessionId())
                .attackerEntityId(attacker.getId())
                .targetEntityId(target.getId())
                .damageCaused(attacker.getAttack())
                .attackerCurrentAP(attacker.getCurrentAP())
                .build();
        publisher.publishEntityAttackEvent(attackEvent);

        EntityStatsUpdatedEvent statsUpdateEvent = EntityStatsUpdatedEvent.builder()
                .sessionId(session.getSessionId())
                .entityId(target.getId())
                .damageToHp(result.getDamageToHp())
                .currentHp(target.getCurrentHp())
                .absorbedByArmor(result.getAbsorbedByArmor())
                .currentDefense(target.getDefense())
                .isDead(target.isDead())
                .build();
        publisher.publishEntityStatsUpdatedEvent(statsUpdateEvent);

        if (result.isDead()) {
            processDeath(session, target);
        }
    }

    private void processDeath(GameSession session, Entity target) {
        log.info("Entity {} died.", target.getId());

        EntityDiedEvent deathEvent = EntityDiedEvent.builder()
                .sessionId(session.getSessionId())
                .entityId(target.getId())
                .build();
        publisher.publishEntityDiedEvent(deathEvent);

        combatService.handleEntityDeath(session, target.getId());
    }

    private void validateTurn(GameSession session, Entity entity) {
        if (entity.getState() != EntityState.COMBAT) {
            return;
        }

        Combat combat = session.findCombatByEntityId(entity.getId())
                .orElseThrow(() -> new GameActionException("Entity in COMBAT state but combat not found", "SYSTEM_ERROR"));

        String currentActorId = combat.getTurnOrder().get(combat.getCurrentTurnIndex());

        if (!currentActorId.equals(entity.getId())) {
            throw new GameActionException("It is not your turn!", "NOT_YOUR_TURN");
        }
    }

    private boolean isTargetInRange(Entity attacker, Entity target) {
        if(attacker == null || target == null)
            return false;

        int attackRange = attacker.getAttackRange();

        int distance = attacker.getPosition().distanceTo(target.getPosition());
        return attackRange >= distance;
    }
}
