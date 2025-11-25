package com.abs.dungeonCrawler.eventcontracts;

import com.abs.dungeonCrawler.eventcontracts.Dto.PlayerStateDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlayerJoinedEvent {
    private String sessionId;
    private PlayerStateDto playerStateDto;
}