package com.abs.dungeonCrawler.eventcontracts.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HexDto {
    private int q;
    private int r;
}
