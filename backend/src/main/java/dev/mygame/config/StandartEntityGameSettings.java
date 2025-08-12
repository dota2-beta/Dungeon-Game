package dev.mygame.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Стандартные настройки игры, связанные с персонажами
 */
@Component
@ConfigurationProperties("game.settings")
@Data
public class StandartEntityGameSettings {
    private int defaultEntityMaxAp;
    private int defaultMonsterMaxAp;
    private int defaultAttackCost;
    private int defaultMovementCost;
    private int defaultCheckRadius;
    private int defaultEntityCurrentAp;
}
