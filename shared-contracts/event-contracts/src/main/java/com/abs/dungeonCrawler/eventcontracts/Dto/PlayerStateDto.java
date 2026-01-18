package com.abs.dungeonCrawler.eventcontracts.Dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerStateDto extends EntityStateDto {
    private String userId;
}
