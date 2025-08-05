package dev.mygame.domain.session;

import dev.mygame.data.templates.AbilityTemplate;
import dev.mygame.enums.EntityStateType;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import dev.mygame.data.templates.AbilityTemplate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Представляет экземпляр способности, принадлежащий конкретной сущности.
 * Это простой класс для хранения данных (POJO), чтобы он был совместим с MapStruct и другими библиотеками.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AbilityInstance {

    private AbilityTemplate template;
    private int turnCooldown;
    private long cooldownEndTime;

    /**
     * Конструктор для удобного создания из шаблона.
     * @param template - шаблон, на основе которого создается способность.
     */
    public AbilityInstance(AbilityTemplate template) {
        if (template == null) {
            throw new IllegalArgumentException("Ability template cannot be null");
        }
        this.template = template;
        this.turnCooldown = 0;
        this.cooldownEndTime = 0;
    }

    /**
     * Проверяет, готово ли заклинание, в зависимости от контекста боя.
     * @param stateType - находится ли юнит в бою.
     */
    public boolean isReady(EntityStateType stateType) {
        if (stateType == EntityStateType.COMBAT) {
            return turnCooldown <= 0;
        } else {
            return System.currentTimeMillis() >= cooldownEndTime;
        }
    }

    /**
     * Уменьшает кулдаун в ходах. Вызывается в начале хода юнита в бою.
     */
    public void reduceTurnCooldown() {
        if (turnCooldown > 0) {
            turnCooldown--;
        }
    }
}
