package dev.mygame.service;

import dev.mygame.domain.factory.EntityFactory;
import dev.mygame.domain.model.Monster;
import dev.mygame.domain.model.map.GameMapHex;
import dev.mygame.domain.session.GameSession;
import dev.mygame.service.internal.SpawnPointInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Сервис, отвечающий за начальное заселение игровой карты монстрами при создании сессии
 * <p>
 * Он считывает предопределенные точки спавна из объекта {@link GameMapHex},
 * определяет по символу какой шаблон монстра использовать,
 * и делегирует создание объекта монстра фабрике {@link EntityFactory}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MonsterSpawnerService {

    private final EntityFactory entityFactory;

    /**
     * Заселяет карту монстрами для указанной игровой сессии.
     * @param session Игровая сессия, которую нужно заселить.
     */
    public void spawnMonsters(GameSession session) {
        GameMapHex map = session.getGameMap();

        if (map == null || map.getMonsterSpawnPoints() == null || map.getMonsterSpawnPoints().isEmpty()) {
            log.warn("No monster spawn points found on the map for session {}. No monsters will be spawned.",
                    session.getSessionID());
            return;
        }

        log.info("Spawning {} monsters for session {}...", map.getMonsterSpawnPoints().size(), session.getSessionID());

        for (SpawnPointInfo spawnInfo : map.getMonsterSpawnPoints()) {
            String monsterTemplateId = resolveMonsterIdBySymbol(spawnInfo.getSymbol());

            if (monsterTemplateId != null) {
                Monster monster = entityFactory.createMonster(monsterTemplateId, spawnInfo.getPosition());
                session.addEntity(monster);
                log.info("Spawned monster '{}' (template: {}) at {}",
                        monster.getName(),
                        monsterTemplateId,
                        spawnInfo.getPosition()
                );
            } else {
                log.warn("Could not resolve a monster template for symbol '{}' at position {}",
                        spawnInfo.getSymbol(),
                        spawnInfo.getPosition()
                );
            }
        }
    }

    /**
     * Превращает символ с карты в ID шаблона монстра.
     * Эта логика определяет, какой монстр появится на месте символа 'M', 'B' и т.д.
     * В будущем эту "карту" можно вынести в отдельный конфигурационный файл.
     *
     * @param symbol Символ из файла карты.
     * @return строковый ID шаблона монстра или null, если символ неизвестен.
     */
    private String resolveMonsterIdBySymbol(char symbol) {
        return switch (symbol) {
            case 'M' -> "goblin_warrior"; // 'M' - это обычный гоблин
            case 'B' -> "lich_king_boss"; // 'B' - это босс
            // case 'O' -> "orc_brute";
            // case 'S' -> "skeleton_archer";
            default -> null;
        };
    }
}
