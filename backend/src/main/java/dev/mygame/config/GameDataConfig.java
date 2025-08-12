package dev.mygame.config;

import dev.mygame.data.templates.AbilityTemplates;
import dev.mygame.data.templates.MonsterClassTemplates;
import dev.mygame.data.templates.PlayerClassTemplates;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * Конфигурационный класс, который отвечает за создание бинов,
 * необходимых для загрузки игровых данных.
 */
@Configuration
public class GameDataConfig {

    @Bean("playerClassTemplates")
    @ConfigurationProperties(prefix = "")
    @Validated
    public PlayerClassTemplates playerClassTemplates() {
        return new PlayerClassTemplates();
    }

    @Bean("monsterClassTemplates")
    @ConfigurationProperties(prefix = "")
    @Validated
    public MonsterClassTemplates monsterTemplates() {
        return new MonsterClassTemplates();
    }

    @Bean("abilityTemplates")
    @ConfigurationProperties(prefix = "")
    @Validated
    public AbilityTemplates abilityTemplates() {
        return new AbilityTemplates();
    }
}
