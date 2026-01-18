package com.abs.dungeonCrawler.eventcontracts.Dto;

import com.abs.dungeonCrawler.eventcontracts.enums.TileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TileDto {
    private HexDto coordinate;
    private TileType type;
}
