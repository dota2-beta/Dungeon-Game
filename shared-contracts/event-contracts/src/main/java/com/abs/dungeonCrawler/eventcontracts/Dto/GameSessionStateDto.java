package com.abs.dungeonCrawler.eventcontracts.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GameSessionStateDto {
    public String sessionId;
    public MapStateDto mapState;
    public List<EntityStateDto> entities;
}
