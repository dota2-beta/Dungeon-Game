package dev.mygame.data;

import dev.mygame.data.templates.*;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GameDataLoader {
    private final PlayerClassTemplates playerClassTemplates;
    private final MonsterClassTemplates monsterClassTemplates;
    private final AbilityTemplates abilityTemplates;

    private Map<String, PlayerClassTemplate> playerClassTemplateMap;
    private Map<String, MonsterClassTemplate> monsterClassTemplateMap;
    private Map<String, AbilityTemplate> abilityTemplateMap;

    @PostConstruct
    public void init() {
        this.playerClassTemplateMap = this.playerClassTemplates.getPlayerClasses().stream()
                .collect(Collectors.toMap(PlayerClassTemplate::getTemplateId, temp -> temp));

        this.monsterClassTemplateMap = this.monsterClassTemplates.getMonsterClasses().stream()
                .collect(Collectors.toMap(MonsterClassTemplate::getTemplateId, temp -> temp));

        this.abilityTemplateMap = this.abilityTemplates.getAbilities().stream()
                .collect(Collectors.toMap(AbilityTemplate::getTemplateId, temp -> temp));
    }

    public Optional<PlayerClassTemplate> getPlayerClassTemplate(String templateId) {
        return Optional.ofNullable(playerClassTemplateMap.get(templateId));
    }

    public Optional<MonsterClassTemplate> getMonsterClassTemplate(String templateId) {
        return Optional.ofNullable(monsterClassTemplateMap.get(templateId));
    }

    public Optional<AbilityTemplate> getAbilityTemplate(String templateId) {
        return Optional.ofNullable(abilityTemplateMap.get(templateId));
    }


    public List<PlayerClassTemplate> getAllPlayerClasses() {
        return playerClassTemplateMap.values().stream().toList();
    }

    public List<AbilityTemplate> getAllAbilities() {
        return  abilityTemplateMap.values().stream().toList();
    }
}
