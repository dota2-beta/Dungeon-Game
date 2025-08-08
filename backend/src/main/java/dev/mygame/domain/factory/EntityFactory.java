package dev.mygame.domain.factory;

import dev.mygame.config.GameSettings;
import dev.mygame.data.GameDataLoader;
import dev.mygame.data.templates.EntityStatsTemplate;
import dev.mygame.data.templates.MonsterClassTemplate;
import dev.mygame.data.templates.PlayerClassTemplate;
import dev.mygame.domain.model.Monster;
import dev.mygame.domain.model.Player;
import dev.mygame.domain.model.map.Hex;
import dev.mygame.domain.session.AbilityInstance;
import dev.mygame.enums.EntityStateType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EntityFactory {
    private final GameDataLoader gameDataLoader;
    private final GameSettings gameSettings;
    /**
     * Создает объект Player на основе шаблона класса.
     * @param classTemplateId ID шаблона
     * @param userId ID пользователя из системы аутентификации
     * @param websocketSessionId ID сессии WebSocket
     * @param startPosition Начальная позиция на карте
     * @return Готовый объект Player или null, если шаблон не найден
     */
    public Player createPlayer(String classTemplateId, String userId, String websocketSessionId, Hex startPosition) {
        PlayerClassTemplate playerClassTemplate = gameDataLoader.getPlayerClassTemplate(classTemplateId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No player class template found with id " + classTemplateId));
        EntityStatsTemplate stats = playerClassTemplate.getStats();

        List<AbilityInstance> playerAbilities = new ArrayList<>();
        if (playerClassTemplate.getAbilities() != null) {
            for (String abilityId : playerClassTemplate.getAbilities()) {
                gameDataLoader.getAbilityTemplate(abilityId)
                        .ifPresent(abilityTemplate -> {
                            playerAbilities.add(new AbilityInstance(abilityTemplate));
                        });
            }
        }

        return Player.builder()
                .id(UUID.randomUUID().toString())
                .position(startPosition)
                .maxHp(stats.getMaxHp())
                .currentHp(stats.getMaxHp())
                .attack(stats.getAttack())
                .defense(stats.getDefense())
                .initiative(stats.getInitiative())
                .maxAP(stats.getMaxAP())
                .currentAP(gameSettings.getDefaultEntityCurrentAp())
                .attackRange(stats.getAttackRange())
                .state(EntityStateType.EXPLORING)
                .userId(userId)
                .websocketSessionId(websocketSessionId)
                .teamId(null)
                .aggroRadius(stats.getAggroRadius())
                .abilities(playerAbilities)
                .name(playerClassTemplate.getName())
                .build();
    }

    /**
     * Создает объект Monster на основе шаблона
     * @param templateId ID шаблона (например, "goblin_warrior")
     * @param position Позиция спавна на карте
     * @return Готовый объект Monster или null, если шаблон не найден
     */
    public Monster createMonster(String templateId, Hex position) {
        MonsterClassTemplate template = gameDataLoader.getMonsterClassTemplate(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Monster template not found: " + templateId));

        EntityStatsTemplate stats = template.getStats();

        List<AbilityInstance> monsterAbilities = new ArrayList<>();
        if(template.getAbilities() != null) {
            for(String abilityId : template.getAbilities()) {
                gameDataLoader.getAbilityTemplate(abilityId)
                        .ifPresent(
                            abilityTemplate -> {
                                monsterAbilities.add(new AbilityInstance(abilityTemplate));
                            }
                        );
            }
        }

        return Monster.builder()
                .id(UUID.randomUUID().toString()) // Генерируем уникальный ID экземпляра
                .name(template.getName())
                .position(position)
                .maxHp(stats.getMaxHp())
                .currentHp(stats.getMaxHp())
                .attack(stats.getAttack())
                .defense(stats.getDefense())
                .initiative(stats.getInitiative())
                .maxAP(stats.getMaxAP())
                .currentAP(stats.getMaxAP())
                .attackRange(stats.getAttackRange())
                .aggroRadius(stats.getAggroRadius())
                .state(EntityStateType.EXPLORING)
                .abilities(monsterAbilities)
                .build();
    }
}
