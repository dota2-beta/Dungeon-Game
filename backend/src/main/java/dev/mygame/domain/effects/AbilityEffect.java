package dev.mygame.domain.effects;

import dev.mygame.service.internal.EffectResult;

import java.util.List;

public interface AbilityEffect {
    /**
     * Применяет логику эффекта к целям.
     * @param context Вся информация о текущем касте.
     */
    List<EffectResult> apply(AbilityContext context);

    /**
     * Возвращает тип эффекта, за который отвечает этот класс.
     * @return строковый идентификатор типа.
     */
    String getEffectType();
}
