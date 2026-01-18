package com.abs.dungeonCrawler.eventcontracts;

import com.abs.dungeonCrawler.eventcontracts.Dto.EntityStateDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EntityJoinedEvent {
    private String sessionId;
    private EntityStateDto entityStateDto;
}
