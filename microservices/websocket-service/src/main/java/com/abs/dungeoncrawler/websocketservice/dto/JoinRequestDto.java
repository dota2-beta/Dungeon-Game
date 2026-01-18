package com.abs.dungeoncrawler.websocketservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JoinRequestDto {
    private String username;
    private String templateId;
    private String sessionId;
}