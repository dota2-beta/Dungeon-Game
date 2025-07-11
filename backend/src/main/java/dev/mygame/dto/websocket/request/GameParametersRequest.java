package dev.mygame.dto.websocket.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameParametersRequest {
    public String mapType;
    public int levelDiffucult;
    public String levelType;
    public List<String> initPlayers;
}
