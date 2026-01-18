package com.abs.dungeoncrawler.websocketservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateSessionRequestDto {
    private String username;
    private String templateId;
    private String level;
}
