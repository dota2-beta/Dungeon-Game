package com.abs.dungeonCrawler.eventcontracts.Dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayerStateDto {
    private String userId;
}
