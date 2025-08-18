package dev.mygame.service;

import dev.mygame.domain.model.Entity;
import org.springframework.stereotype.Service;

/**
 * Сервис для определения взаимоотношений между различными персонажами
 * <p>
 * Предоставляет единый метод {@link #areEnemies} для определения, являются ли
 * две сущности врагами на основе их {@code teamId}
 */
@Service
public class FactionService {
    public boolean areEnemies(Entity entity1, Entity entity2) {
        if(entity1.equals(entity2))
            return false;

        if (entity1.getTeamId() == null || entity2.getTeamId() == null) {
            // если у кого-то нет команды, то они враждебны всем
            return true;
        }
        // команды враждебны, если их ID команды не совпадают
        return !entity1.getTeamId().equals(entity2.getTeamId());
    }
}
