package com.abs.dungeonCrawler.eventcontracts.eventDto;

import com.abs.dungeonCrawler.eventcontracts.Dto.HexDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntityMovedEvent {
    private String sessionId;
    private String entityId;    // Кто походил
    private HexDto newPosition; // Куда встал
    private int currentAP;
}