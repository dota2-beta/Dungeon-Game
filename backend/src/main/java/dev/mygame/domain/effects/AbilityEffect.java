package dev.mygame.domain.effects;

public interface AbilityEffect {
    /**
     * Применяет логику эффекта к целям.
     * @param context Вся информация о текущем касте.
     */
    void apply(AbilityContext context);

    /**
     * Возвращает тип эффекта, за который отвечает этот класс.
     * @return строковый идентификатор типа.
     */
    String getEffectType();
}
