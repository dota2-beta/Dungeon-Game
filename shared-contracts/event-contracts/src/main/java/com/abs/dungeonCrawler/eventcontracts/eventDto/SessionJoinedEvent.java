package com.abs.dungeonCrawler.eventcontracts.eventDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SessionJoinedEvent {
    private String sessionId;
    private String userId;
}
