package com.abs.dungeoncrawler.gamesessionservice.services;

import com.abs.dungeoncrawler.gamesessionservice.domain.GameSession;
import com.abs.dungeoncrawler.gamesessionservice.domain.model.Monster;
import com.abs.dungeoncrawler.gamesessionservice.domain.model.map.GameMapHex;
import com.abs.dungeoncrawler.gamesessionservice.domain.model.map.SpawnPointInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MonsterSpawner {
    private final EntityFactory entityFactory;

    public void spawnMonsters(GameSession gameSession) {
        GameMapHex gameMap = gameSession.getGameMap();
        List<SpawnPointInfo> monsterSpawnPoints = gameMap.getMonsterSpawnPoints();
        for(var spawnPoint : monsterSpawnPoints){
            Monster monster = entityFactory.createMonster(spawnPoint.getTemplateId());
            monster.setPosition(spawnPoint.getPosition());
            gameSession.addEntity(monster);
        }
    }
}
