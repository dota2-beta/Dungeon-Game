package com.abs.dungeoncrawler.websocketservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RespondToPeaceRequestDto {
    private String sessionId;
    private boolean accept;
}
