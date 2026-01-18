package com.abs.dungeoncrawler.gamesessionservice.services;

import com.abs.dungeoncrawler.gamesessionservice.config.StandartEntityGameSettings;
import com.abs.dungeoncrawler.gamesessionservice.domain.GameSession;
import com.abs.dungeoncrawler.gamesessionservice.domain.model.Entity;
import com.abs.dungeoncrawler.gamesessionservice.domain.model.map.Hex;
import com.abs.dungeoncrawler.gamesessionservice.domain.model.map.Tile;
import com.abs.dungeoncrawler.gamesessionservice.exception.SessionNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Реализует логику искусственного интеллекта для монстров в бою.
 * Отвечает за выбор цели и последовательность действий во время хода монстра.
 */
@Service
//@RequiredArgsConstructor
@Slf4j
public class AIService {

    private final SessionRepository sessionRepository;

    //@Lazy
    private final CoreGameEngine coreGameEngine;

    private final CombatService combatService;
    private final FactionService factionService;
    private final StandartEntityGameSettings settings;

    public AIService(
            SessionRepository sessionRepository,
            @Lazy CoreGameEngine coreGameEngine,
            @Lazy CombatService combatService,
            FactionService factionService,
            StandartEntityGameSettings settings
    ) {
        this.sessionRepository = sessionRepository;
        this.coreGameEngine = coreGameEngine;
        this.combatService = combatService;
        this.factionService = factionService;
        this.settings = settings;
    }

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    /**
     * Точка входа. Вызывается из CombatService, когда ход переходит к монстру.
     */
    public void scheduleTurn(String sessionId, String monsterId) {
        scheduler.schedule(() -> executeMonsterTurn(sessionId, monsterId), 1000, TimeUnit.MILLISECONDS);
    }

    /**
     * Основной цикл AI.
     */
    private void executeMonsterTurn(String sessionId, String monsterId) {
        try {
            GameSession session = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new SessionNotFoundException("Session " + sessionId + " not found"));

            Entity monster = session.getEntityById(monsterId);

            if (monster == null || monster.isDead() || monster.getCurrentAP() <= 0) {
                endTurn(session, monsterId);
                return;
            }

            List<Entity> enemies = findVisibleEnemies(session, monster);
            if (enemies.isEmpty()) {
                endTurn(session, monsterId);
                return;
            }

            String bestTargetId = selectBestTargetId(monster, enemies);
            if (bestTargetId == null) {
                endTurn(session, monsterId);
                return;
            }

            boolean actionTaken = performNextAction(session, monster, bestTargetId);

            if (actionTaken) {
                scheduler.schedule(() -> executeMonsterTurn(sessionId, monsterId), 500, TimeUnit.MILLISECONDS);
            } else {
                endTurn(session, monsterId);
            }

        } catch (Exception e) {
            log.error("Error during AI turn execution for monster {}", monsterId, e);
            try {
                sessionRepository.findById(sessionId).ifPresent(session -> endTurn(session, monsterId));
            } catch (Exception ex) {
                log.error("Failed to force end turn for AI", ex);
            }
        }
    }

    private void endTurn(GameSession session, String monsterId) {
        log.info("AI ending turn for {}", monsterId);
        combatService.requestEndTurn(session, monsterId);
    }

    /**
     * Пытается атаковать или подойти.
     */
    private boolean performNextAction(GameSession session, Entity monster, String targetId) throws Exception {
        Entity target = session.getEntityById(targetId);
        if (target == null || target.isDead()) {
            return false;
        }

        int distanceToTarget = monster.getPosition().distanceTo(target.getPosition());
        int attackRange = monster.getAttackRange();
        int attackCost = settings.getDefaultAttackCost();

        if (distanceToTarget <= attackRange) {
            if (monster.getCurrentAP() >= attackCost) {
                coreGameEngine.handleAttackAction(session, monster.getId(), target.getId());
                return true;
            } else {
                return false;
            }
        }

        int moveCost = settings.getDefaultMovementCost();
        if (monster.getCurrentAP() >= moveCost) {
            Hex movementTargetHex = findBestAdjacentHex(session, monster, target);

            if (movementTargetHex == null) {
                return false;
            }

            List<Hex> path = session.getGameMap().findPath(
                    monster.getPosition(),
                    movementTargetHex,
                    session.getEntities().values(),
                    monster.getId()
            );

            if (path != null && path.size() > 1) {
                Hex nextStep = path.get(1);
                coreGameEngine.handleMoveAction(session, monster.getId(), nextStep);
                return true;
            }
        }

        return false;
    }

    private Hex findBestAdjacentHex(GameSession session, Entity self, Entity target) {
        Hex targetPosition = target.getPosition();

        List<Hex> validAdjacentHexes = targetPosition.getNeighbors().stream()
                .filter(hex -> {
                    Tile tile = session.getGameMap().getTile(hex);
                    return tile != null && tile.isPassable() && !tile.isOccupied();
                })
                .toList();

        if (validAdjacentHexes.isEmpty()) {
            return null;
        }

        return validAdjacentHexes.stream()
                .min(Comparator.comparingInt(hex -> self.getPosition().distanceTo(hex)))
                .orElse(null);
    }

    private List<Entity> findVisibleEnemies(GameSession session, Entity self) {
        return session.getEntities().values().stream()
                .filter(entity -> !entity.isDead())
                .filter(e -> !e.getId().equals(self.getId()))
                .filter(e -> factionService.areEnemies(self, e))
                .toList();
    }

    private String selectBestTargetId(Entity self, List<Entity> enemies) {
        if (enemies == null || enemies.isEmpty()) {
            return null;
        }
        return enemies.stream()
                .filter(e -> e.getPosition() != null)
                .min(Comparator.comparingInt(e -> self.getPosition().distanceTo(e.getPosition())))
                .map(Entity::getId)
                .orElse(null);
    }
}