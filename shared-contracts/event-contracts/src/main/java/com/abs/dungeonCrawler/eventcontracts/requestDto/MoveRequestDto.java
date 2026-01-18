package com.abs.dungeonCrawler.eventcontracts.requestDto;

import com.abs.dungeonCrawler.eventcontracts.Dto.HexDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoveRequestDto {
    private String sessionId;
    private HexDto target; // Координата назначения
}