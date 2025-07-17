package dev.mygame.service;

import dev.mygame.domain.model.Entity;
import org.springframework.stereotype.Service;

@Service
public class FactionService {
    public boolean areEnemies(Entity entity1, Entity entity2) {
        if (entity1.getTeamId() == null || entity2.getTeamId() == null) {
            // Если у кого-то нет команды, они враждебны всем
            return true;
        }
        // Команды враждебны, если их ID не совпадают
        return !entity1.getTeamId().equals(entity2.getTeamId());
    }
}
