package dev.mygame.game.service;

import dev.mygame.config.MapGenerationProperties;
import dev.mygame.domain.model.Entity;
import dev.mygame.domain.model.Monster;
import dev.mygame.domain.model.Player;
import dev.mygame.domain.model.map.GameMapHex;
import dev.mygame.domain.model.map.Hex;
import dev.mygame.domain.model.map.Tile;
import dev.mygame.enums.TileType;
import dev.mygame.service.MapGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*; // Импортируем статические методы для утверждений

/**
 * Юнит-тесты для базовой логики гексагональной сетки.
 * Этот тест не использует Spring Context, он тестирует чистую Java логику.
 */
class HexLogicTest {

    private GameMapHex battleMap;
    private Entity player;
    private Entity monster;

    @BeforeEach
    void setUp() {
        System.out.println("Setting up new test...");
        MapGenerationProperties testProps = new MapGenerationProperties();
        testProps.setBattleArenaRadius(10);
        MapGenerator mapGenerator = new MapGenerator();
        this.battleMap = mapGenerator.generateHexBattleArena(testProps);

        this.player = Player.builder()
                .name("Hero")
                .attackRange(2)
                .currentHp(100) // Задаем и другие важные поля
                .maxHp(100)
                .build();

        this.monster = Monster.builder()
                .name("Goblin")
                .attackRange(1)
                .currentHp(30)
                .maxHp(30)
                .build();
    }

    @Test
    @DisplayName("Проверка: Атака возможна, если цель в радиусе")
    void checkAttackRange_ShouldReturnTrue_WhenTargetIsInRange() {
        Hex playerPos = new Hex(0, 0);
        Hex monsterPos = new Hex(2, -1); // Расстояние = 2

        int distance = playerPos.distanceTo(monsterPos);
        boolean canAttack = distance <= player.getAttackRange();

        assertTrue(canAttack, "Герой должен мочь атаковать, т.к. дистанция (2) <= радиус атаки (2)");
    }

    @Test
    @DisplayName("Проверка: Атака невозможна, если цель вне радиуса")
    void checkAttackRange_ShouldReturnFalse_WhenTargetIsOutOfRange() {
        Hex playerPos = new Hex(0, 0);
        Hex monsterPos = new Hex(3, -1); // Расстояние = 3

        int distance = playerPos.distanceTo(monsterPos);
        boolean canAttack = distance <= player.getAttackRange();

        assertFalse(canAttack, "Герой не должен мочь атаковать, т.к. дистанция (3) > радиус атаки (2)");
    }

    @Test
    @DisplayName("Проверка: Перемещение на свободную проходимую клетку успешно")
    void move_ShouldSucceed_ToEmptyWalkableTile() {
        Hex startPos = new Hex(0, 0);
        Hex endPos = new Hex(1, 0); // Соседняя клетка
        boolean isWalkable = battleMap.getTile(endPos) != null && battleMap.getTile(endPos).isPassable();

        assertTrue(isWalkable, "Перемещение на соседний тайл должно быть успешным");
    }

    @Test
    @DisplayName("Проверка: Перемещение на несуществующую клетку невозможно")
    void move_ShouldFail_ToNonExistentTile() {
        Hex outsidePos = new Hex(10, 10);

        boolean tileExists = battleMap.getTile(outsidePos) != null;

        assertFalse(tileExists, "Перемещение за пределы карты должно быть невозможным");
    }

    @Test
    @DisplayName("Проверка: возврат пути при перемещении игрока")
    void move_ShouldSucceed_FromHexToHex() {
        GameMapHex gameMap = new GameMapHex();

        Hex startPos = new Hex(0, 0);
        Hex endPos = new Hex(3, -2);

        int mapRadius = 5;
        for (int q = -mapRadius; q <= mapRadius; q++) {
            for (int r = -mapRadius; r <= mapRadius; r++) {
                Hex hex = new Hex(q, r);
                if (hex.distanceTo(new Hex(0,0)) <= mapRadius) {
                    gameMap.setTile(hex, new Tile(TileType.FLOOR, null));
                }
            }
        }

        List<Hex> path = gameMap.findPath(startPos, endPos);

        assertNotNull(path, "Путь не должен быть null");
        assertFalse(path.isEmpty(), "Путь не должен быть пустым");

        int expectedDistance = startPos.distanceTo(endPos);
        assertEquals(expectedDistance + 1, path.size(), "Длина пути должна быть равна расстоянию + 1");

        // Проверка 3: Путь начинается и заканчивается там, где нужно?
        assertEquals(startPos, path.get(0), "Путь должен начинаться со стартовой точки");
        assertEquals(endPos, path.get(path.size() - 1), "Путь должен заканчиваться в целевой точке");

        System.out.println("Найденный путь: " + path);
    }
}