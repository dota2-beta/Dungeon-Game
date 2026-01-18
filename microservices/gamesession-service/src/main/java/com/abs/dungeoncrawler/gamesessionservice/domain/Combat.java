package com.abs.dungeoncrawler.gamesessionservice.domain;

import com.abs.dungeoncrawler.gamesessionservice.domain.model.Entity;
import com.abs.dungeonCrawler.eventcontracts.enums.EntityState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Combat {
    private String id;
    private Set<String> participantIds;
    private List<String> turnOrder;
    private int currentTurnIndex;

    @Builder.Default
    private Map<String, Boolean> peaceVotes = null;
}
