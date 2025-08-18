package dev.mygame.enums;

public enum AbilityUseResultEnum {
    SUCCESS,                // всё успешно
    NOT_ENOUGH_AP,          // недостаточно ap
    ON_COOLDOWN,            // способность на кулдауне
    OUT_OF_RANGE,           // цель слишком далеко
    INVALID_TARGET,         // неподходящая цель
    CASTER_IS_DEAD          // кастер мертв
}
