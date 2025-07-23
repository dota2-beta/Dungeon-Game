package dev.mygame.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.mygame.service.internal.DamageResult;
import dev.mygame.enums.EntityStateType;
import dev.mygame.domain.event.DeathListener;
import jakarta.annotation.Nullable;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public abstract class Entity extends GameMapObject {
    protected String name;
    protected int currentHp;
    protected int maxHp;
    protected int attack;
    protected int defense;
    protected int attackRange;
    private int currentAP;
    private int maxAP;
    private int initiative;
    private int aggroRadius;

    protected String teamId; // Id команды, к которой принадлежит Entity
    protected String websocketSessionId;

    private EntityStateType state;

    @Builder.Default
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<DeathListener> deathListener = new ArrayList<>();
    //@JsonProperty("isDead")
    private boolean isDead = false;

    public DamageResult takeDamage(int damage) {
        if (this.isDead || damage <= 0) {
            return new DamageResult(0, 0, 0, this.isDead);
        }

        int absorbedByArmor = 0;
        if (this.defense > 0) {
            absorbedByArmor = Math.min(damage, this.defense);
            this.defense -= absorbedByArmor;
        }
        int damageToHp = damage - absorbedByArmor;

        if (damageToHp > 0) {
            this.currentHp -= damageToHp;
        }

        boolean justDied = false;
        if (this.currentHp <= 0) {
            this.currentHp = 0;

            if (!this.isDead) {
                this.isDead = true;
                justDied = true;
            }
        }

        if (justDied) {
            notifyDeathListeners();
        }

        return new DamageResult(damage, absorbedByArmor, damageToHp, this.isDead);
    }

    public boolean isAlive(){
        return this.currentHp > 0;
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
        if (this.deathListener == null || this.deathListener.isEmpty())
            return;

        List<DeathListener> listenersCopy = new ArrayList<>(this.deathListener);
        for(DeathListener listener : listenersCopy) {
            listener.onEntityDied(this);
        }
        this.deathListener.clear();
    }
}
