package dev.mygame.controller.websocket;

import dev.mygame.data.GameDataLoader;
import dev.mygame.data.templates.AbilityTemplate;
import dev.mygame.data.templates.PlayerClassTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/game-data")
@RequiredArgsConstructor
public class GameDataController {

    private final GameDataLoader gameDataLoader;

    @GetMapping("/player-classes")
    public List<PlayerClassTemplate> getPlayerClasses() {
        return gameDataLoader.getAllPlayerClasses();
    }

    @GetMapping("/abilities")
    public List<AbilityTemplate> getAbilities() {
        return gameDataLoader.getAllAbilities();
    }
}