package com.abs.dungeonCrawler.eventcontracts.eventDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Builder
@NoArgsConstructor
@Data
@AllArgsConstructor
public class CombatTeamDto {
    private String teamId;
    private Set<String> memberIds;
}
