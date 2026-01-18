package com.abs.dungeoncrawler.gamesessionservice.domain.model;

import com.abs.dungeonCrawler.eventcontracts.enums.EntityState;
import com.abs.dungeoncrawler.gamesessionservice.domain.DamageResult;
import com.abs.dungeoncrawler.gamesessionservice.domain.HealResult;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class Entity extends GameMapObject {
    private String name;
    private int currentHp;
    private int maxHp;
    private int attack;
    private int defense;
    private int attackRange;
    private int currentAP;
    private int maxAP;
    private int initiative;
    private int aggroRadius;
    private EntityState state;
    private boolean isDead;
    private String teamId; // Id команды, к которой принадлежит Entity

    public DamageResult takeDamage(int damage) {
        if (this.isDead || damage <= 0) {
            return new DamageResult(this.getId(), 0, 0, 0, this.isDead);
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

        if (this.currentHp <= 0) {
            this.currentHp = 0;
            this.isDead = true;
        }

        return new DamageResult(this.getId(), damage, absorbedByArmor, damageToHp, this.isDead);
    }

    public HealResult takeHeal(int healAmount) {
        if (this.isDead || healAmount <= 0) {
            return new HealResult(this.getId(), 0, this.currentHp);
        }
        int oldHp = this.currentHp;
        int newHp = this.currentHp + healAmount;
        this.currentHp = Math.min(newHp, this.maxHp);
        int actualHealedAmount = this.currentHp - oldHp;

        return new HealResult(this.getId(), actualHealedAmount, this.currentHp);
    }

//    public boolean isAlive(){
//        return this.currentHp > 0;
//    }
}
