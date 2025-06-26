package dev.mygame.game.model;

import dev.mygame.dto.DamageResult;
import dev.mygame.game.enums.EntityState;
import dev.mygame.game.session.event.DeathListener;
import dev.mygame.game.model.map.Point;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public abstract class Entity extends GameMapObject {
    protected String id;
    protected String name;
    protected int currentHp;
    protected int maxHp;
    protected int attack;
    protected int defense;
    private int currentAP;
    private int maxAP;
    private int initiative;

    protected Point position;

    private EntityState state;
    private List<DeathListener> deathListener;
    private boolean isDead = false;

    public DamageResult takeDamage(int damage) {
        if(damage <= 0) {
            return new DamageResult(0, 0, 0);
        }
        int effectiveDamage = damage;
        int absorbedByArmor = 0;
        if(this.defense > 0)
        {
            absorbedByArmor = Math.min(effectiveDamage, this.defense);
            effectiveDamage -= this.defense;
            this.defense -= absorbedByArmor;
        }
        if(effectiveDamage > 0)
            this.currentHp -= effectiveDamage;
        if(currentHp < 0) {
            currentHp = 0;
        }

        if(!this.isAlive() && !isDead)
            notifyDeathListeners();

        return new DamageResult(damage, absorbedByArmor, effectiveDamage);
    }

    public boolean isAlive(){
        return currentHp > 0;
    }

    public void addDeathListener(DeathListener listener) {
        if(listener != null) {
            this.deathListener.add(listener);
        }
    }

    public void removeDeathListener(DeathListener listener) {
        if(listener != null) {
            this.deathListener.remove(listener);
        }
    }

    private void notifyDeathListeners() {
        for(DeathListener listener : this.deathListener) {
            this.isDead = true;
            listener.onEntityDied(this);
        }
        deathListener.clear();
    }
}
