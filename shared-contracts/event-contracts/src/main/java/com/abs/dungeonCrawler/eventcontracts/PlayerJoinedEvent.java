package com.abs.dungeonCrawler.eventcontracts;

import com.abs.dungeonCrawler.eventcontracts.Dto.PlayerStateDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class PlayerJoinedEvent {
    private String sessionId;
    private PlayerStateDto playerStateDto;
}