package dev.mygame.data.templates;

import lombok.Data;

import java.util.List;
import java.util.Objects;

@Data
public class AbilityTemplate {
    private String templateId;
    private String name;
    private String description;
    private int costAp;
    private int cooldown;
    private int range;
    private String targetType;
    private int areaOfEffectRadius;
    private List<EffectTemplate> effects;

    public boolean hasDamageEffect() {
        if(effects == null || effects.isEmpty()) {
            return false;
        }
        return effects.stream()
                .anyMatch(effect -> Objects.equals(effect.getType(), "DAMAGE"));
    }
}
