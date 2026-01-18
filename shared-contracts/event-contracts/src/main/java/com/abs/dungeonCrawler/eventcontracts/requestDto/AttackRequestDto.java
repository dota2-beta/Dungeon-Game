package com.abs.dungeonCrawler.eventcontracts.requestDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttackRequestDto {
    private String sessionId;
    private String attackerId;
    private String targetId;
}
