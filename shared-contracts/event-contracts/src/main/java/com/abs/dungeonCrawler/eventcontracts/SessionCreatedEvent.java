package com.abs.dungeonCrawler.eventcontracts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SessionCreatedEvent {
    private String sessionId;
    private String userId;
}
