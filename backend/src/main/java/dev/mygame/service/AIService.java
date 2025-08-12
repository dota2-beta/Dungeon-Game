package dev.mygame.service;

import dev.mygame.domain.model.Entity;
import dev.mygame.domain.model.Monster;
import dev.mygame.domain.model.map.Hex;
import dev.mygame.domain.model.map.Tile;
import dev.mygame.domain.session.GameSession;
import dev.mygame.enums.ActionType;
import dev.mygame.service.internal.EntityAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AIService {
    public void executeMonsterTurn(Monster monster, GameSession session) {
        final Map<String, Entity> worldSnapshot = session.getEntities();

        List<Entity> enemies = findVisibleEnemies(monster, worldSnapshot, session.getFactionService());

        if (enemies.isEmpty()) {
            session.endTurn(monster.getId());
            return;
        }
        String bestTargetId = selectBestTargetId(monster, enemies);

        if (bestTargetId == null) {
            session.endTurn(monster.getId());
            return;
        }

        Entity target = worldSnapshot.get(bestTargetId);

        if (target == null) {
            session.endTurn(monster.getId());
            return;
        }

        while (monster.getCurrentAP() > 0) {
            boolean actionTaken = performNextAction(monster, target, session);
            if (!actionTaken) {
                break;
            }
            try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }

        System.out.println("--- AI TURN END for: " + monster.getName() + " ---");
        session.endTurn(monster.getId());
    }

    /**
     * Выполняет одно  действие (атака или движение) для монстра.
     * Передает действие в GameSession для унифицированной обработки.
     * @param monster Актуальное состояние монстра, который совершает действие.
     * @param target Актуальное состояние цели.
     * @param session Игровая сессия.
     * @return true, если действие было успешно инициировано, иначе false.
     */
    private boolean performNextAction(Monster monster, Entity target, GameSession session) {
        int distanceToTarget = monster.getPosition().distanceTo(target.getPosition());
        int attackRange = monster.getAttackRange();
        int attackCost = session.getStandartEntityGameSettings().getDefaultAttackCost();

        if (distanceToTarget <= attackRange && monster.getCurrentAP() >= attackCost) {

            EntityAction attackAction = EntityAction.builder()
                    .actionType(ActionType.ATTACK)
                    .targetId(target.getId())
                    .build();

            session.handleEntityAction(monster.getId(), attackAction);
            return true;
        }

        int moveCost = session.getStandartEntityGameSettings().getDefaultMovementCost();
        if (monster.getCurrentAP() >= moveCost) {
            Hex movementTargetHex = findBestAdjacentHex(monster, target, session);

            if (movementTargetHex == null) {
                return false;
            }
            List<Hex> path = session.getGameMap()
                    .findPath(monster.getPosition(),
                            movementTargetHex,
                            session.getEntities().values(),
                            monster.getId()
                    );

            if (path != null && path.size() > 1) {
                Hex nextStep = path.get(1);
                EntityAction moveAction = EntityAction.builder()
                        .actionType(ActionType.MOVE)
                        .targetHex(nextStep)
                        .build();

                session.handleEntityAction(monster.getId(), moveAction);
                return true;
            } else {
                // TODO: сделать логи
            }
        } else {
            // TODO: сделать логи
        }

        return false;
    }

    /**
     * Находит лучшую соседнюю с целью клетку, на которую может встать монстр.
     * "Лучшая" - значит, самая близкая к текущей позиции монстра.
     * @param monster Монстр, который ищет, куда пойти.
     * @param target Цель, к которой нужно подойти.
     * @param session Игровая сессия для проверки проходимости.
     * @return Координата лучшей соседней клетки или null, если все заняты/непроходимы.
     */
    private Hex findBestAdjacentHex(Monster monster, Entity target, GameSession session) {
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

        // выбираем клетку, которая ближе всего к монстру
        return validAdjacentHexes.stream()
                .min(Comparator.comparingInt(hex -> monster.getPosition().distanceTo(hex)))
                .orElse(null);
    }

    /**
     * Находит видимых врагов
     */
    private List<Entity> findVisibleEnemies(Monster monster, Map<String, Entity> worldState, FactionService factionService) {
        return worldState.values().stream()
                .filter(Entity::isAlive)
                .filter(e -> !e.getId().equals(monster.getId()))
                .filter(e -> factionService.areEnemies(monster, e))
                .toList();
    }

    /**
     * Выбирает ID лучшей цели из списка врагов.
     */
    private String selectBestTargetId(Monster monster, List<Entity> enemies) {
        if (enemies == null || enemies.isEmpty()) {
            return null;
        }

        return enemies.stream()
                .filter(e -> e.getPosition() != null)
                .min(Comparator.comparingInt(e -> monster.getPosition().distanceTo(e.getPosition())))
                .map(Entity::getId)
                .orElse(null);
    }
}
