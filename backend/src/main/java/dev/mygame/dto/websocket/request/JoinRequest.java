package dev.mygame.dto.websocket.request;

import lombok.Data;

@Data
public class JoinRequest {
    public String sessionId;
    public String userId;
}
