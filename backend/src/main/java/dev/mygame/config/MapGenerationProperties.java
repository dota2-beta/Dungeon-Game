package dev.mygame.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "game.map.generation")
@Getter
@Setter
public class MapGenerationProperties {
    private int battleArenaRadius;
    private int minRooms;
    private int maxRooms;
    private int minRoomSize;
    private int maxRoomSize;
}
