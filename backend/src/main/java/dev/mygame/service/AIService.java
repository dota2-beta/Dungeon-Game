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
import java.util.concurrent.TimeUnit;

/**
 * Реализует логику искусственного интеллекта для монстров в бою.
 * Отвечает за выбор цели и последовательность действий во время хода монстра.
 */
@Service
@RequiredArgsConstructor
public class AIService {
    /**
     * Передать управление монстру и выполнить логику AI
     * @param monsterId уникальный идентификатор монстра в рамках сессии
     * @param session сессия, в которой необходимо сделать ход монстру
     */
    public void executeMonsterTurn(String monsterId, GameSession session) {
        Entity currentMonsterState = session.getEntities().get(monsterId);

        if (currentMonsterState == null || currentMonsterState.isDead() || currentMonsterState.getCurrentAP() <= 0) {
            session.endTurn(monsterId);
            return;
        }
        List<Entity> enemies = findVisibleEnemies(currentMonsterState, session.getEntities(), session.getFactionService());
        if (enemies.isEmpty()) {
            session.endTurn(monsterId);
            return;
        }

        String bestTargetId = selectBestTargetId(currentMonsterState, enemies);
        if (bestTargetId == null) {
            session.endTurn(monsterId);
            return;
        }
        boolean actionTaken = performNextAction(currentMonsterState.getId(), bestTargetId, session);

        if (actionTaken) {
            session.getScheduler().schedule(() -> executeMonsterTurn(monsterId, session), 500, TimeUnit.MILLISECONDS);
        } else {
            session.endTurn(monsterId);
        }
    }

    /**
     * Выполняет одно действие (пока что атака или движение) для монстра.
     * Передает действие в GameSession для унифицированной обработки.
     * @param monsterId Актуальное состояние монстра, который совершает действие.
     * @param targetId Актуальное состояние цели.
     * @param session Игровая сессия.
     * @return true, если действие было успешно инициировано, иначе false.
     */
    private boolean performNextAction(String monsterId, String targetId, GameSession session) {
        Entity monster = session.getEntities().get(monsterId);
        Entity target = session.getEntities().get(targetId);

        if (monster == null || target == null) {
            return false;
        }

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
     * Находит лучшую соседнюю с целью клетку, на которую может встать атакующая сущность.
     * "Лучшая" - значит, самая близкая к текущей позиции атакующего.
     * @param self Сущность, которая ищет, куда пойти.
     * @param target Цель, к которой нужно подойти.
     * @param session Игровая сессия для проверки проходимости.
     * @return Координата лучшей соседней клетки или null, если все заняты/непроходимы.
     */
    private Hex findBestAdjacentHex(Entity self, Entity target, GameSession session) {
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

    /**
     * Находит видимых врагов
     * @param self Монстр или игрок, для которого ищем врагов.
     * @param worldState Текущее состояние всех сущностей в игре.
     * @param factionService Сервис для проверки фракций.
     * @return Список враждебных сущностей.
     */
    private List<Entity> findVisibleEnemies(Entity self, Map<String, Entity> worldState, FactionService factionService) {
        return worldState.values().stream()
                .filter(Entity::isAlive) // Ищем только живых
                .filter(e -> !e.getId().equals(self.getId())) // Исключаем самого себя
                .filter(e -> factionService.areEnemies(self, e)) // Фильтруем по враждебности
                .toList();
    }

    /**
     * Выбирает ID лучшей цели из списка врагов.
     * "Лучшая" цель - самая близкая по расстоянию.
     * @param self Сущность, которая выбирает цель.
     * @param enemies Список потенциальных целей.
     * @return ID лучшей цели или null, если список пуст.
     */
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
