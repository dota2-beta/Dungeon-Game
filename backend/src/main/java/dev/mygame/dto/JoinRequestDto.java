package dev.mygame.dto;

import lombok.Data;

@Data
public class JoinRequestDto {
    public String sessionId;
    public String userId;
}
