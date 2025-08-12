package dev.mygame.dto.websocket.request;

import jdk.jfr.Description;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO для получения параметров новой игровой сессии от клиента.
 * Содержит информацию, необходимую для генерации и настройки игрового мира.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameParametersRequest {
    public String mapType;
    public int levelDifficult;
    public String levelType;
    public List<String> initPlayers;
}
