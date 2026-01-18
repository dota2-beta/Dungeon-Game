package com.abs.dungeoncrawler.gamesessionservice.mapper;

import com.abs.dungeonCrawler.eventcontracts.Dto.EntityStateDto;
import com.abs.dungeonCrawler.eventcontracts.Dto.HexDto;
import com.abs.dungeonCrawler.eventcontracts.Dto.PlayerStateDto;
import com.abs.dungeoncrawler.gamesessionservice.domain.model.Entity;
import com.abs.dungeoncrawler.gamesessionservice.domain.model.Monster;
import com.abs.dungeoncrawler.gamesessionservice.domain.model.map.Hex;
import com.abs.dungeoncrawler.gamesessionservice.domain.model.Player;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EntityMapper {
    default HexDto hexToHexDto(Hex hex) {
        if (hex == null) {
            return null;
        }
        return HexDto.builder()
                .q(hex.getQ())
                .r(hex.getR())
                .build();
    }
    default Hex hexDtoToHex(HexDto hexDto) {
        if (hexDto == null) {
            return null;
        }
        return Hex.builder()
                .q(hexDto.getQ())
                .r(hexDto.getR())
                .build();
    }

    @Mapping(target = "type", constant = "PLAYER")
    PlayerStateDto toPlayerState(Player player);
    @Mapping(target = "type", expression = "java(determineType(entity))")
    EntityStateDto toEntityState(Entity entity);

    default String determineType(Entity entity) {
        if (entity instanceof Player) return "PLAYER";
        if (entity instanceof Monster) return "MONSTER";
        return "UNKNOWN";
    }
}
