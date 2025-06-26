package dev.mygame.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("game.settings")
@Data
public class GameSettings {
    private int defaultPlayerMaxAp;
    private int defaultMonsterMaxAp;

    private double moveCostHv;
    private double moveCostDiag;
    private int defaultAttackCost;

    private int defaultMapWidth;
    private int defaultMapHeight;

}
