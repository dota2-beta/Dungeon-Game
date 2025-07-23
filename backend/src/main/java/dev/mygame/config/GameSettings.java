package dev.mygame.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("game.settings")
@Data
public class GameSettings {
    private int defaultEntityMaxAp;
    private int defaultMonsterMaxAp;
    private int defaultAttackCost;
    private int defaultMovementCost;
    private int defaultCheckRadius;
    private int defaultEntityCurrentAp;
}
