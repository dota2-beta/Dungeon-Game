package com.abs.dungeoncrawler.websocketservice.mapper;

import com.abs.dungeonCrawler.eventcontracts.Dto.*;
import com.abs.dungeonCrawler.eventcontracts.enums.EntityState;
import com.abs.dungeonCrawler.eventcontracts.enums.TileType;
import com.dungeoncrawler.contracts.grpc.common.HexMsg;
import com.dungeoncrawler.contracts.grpc.common.TileMsg;
import com.dungeoncrawler.contracts.grpc.gamesession.EntityStateMsg;
import com.dungeoncrawler.contracts.grpc.gamesession.GameSessionStateMsg;
import com.dungeoncrawler.contracts.grpc.gamesession.MapStateMsg;
import org.springframework.stereotype.Component;

@Component
public class ProtoToDtoMapper {
    public GameSessionStateDto toDto(GameSessionStateMsg msg) {
        return GameSessionStateDto.builder()
                .sessionId(msg.getSessionId())
                .mapState(toMapStateDto(msg.getMapState()))
                .entities(msg.getEntitiesList().stream()
                        .map(this::toEntityStateDto)
                        .toList())
                .build();
    }

    private MapStateDto toMapStateDto(MapStateMsg msg) {
        return MapStateDto.builder()
                .tiles(msg.getTilesList().stream()
                        .map(this::toTileDto)
                        .toList())
                .build();
    }

    private TileDto toTileDto(TileMsg msg) {
        return TileDto.builder()
                .coordinate(toHexDto(msg.getCoordinate()))
                .type(TileType.valueOf(msg.getType())) // String -> Enum
                .build();
    }

    private EntityStateDto toEntityStateDto(EntityStateMsg msg) {
        if ("PLAYER".equals(msg.getType()))
            return PlayerStateDto.builder()
                    .id(msg.getId())
                    .name(msg.getName())
                    .position(toHexDto(msg.getPosition()))
                    .currentHp(msg.getCurrentHp())
                    .maxHp(msg.getMaxHp())
                    .currentAP(msg.getCurrentAp())
                    .maxAP(msg.getMaxAp())
                    .defense(msg.getDefense())
                    .type(msg.getType())
                    .teamId(msg.getTeamId())
                    .state(EntityState.valueOf(msg.getState())) // String -> Enum
                    .userId(msg.hasUserId() ? msg.getUserId() : null)
                    .build();
        else
            return EntityStateDto.builder()
                .id(msg.getId())
                .name(msg.getName())
                .position(toHexDto(msg.getPosition()))
                .currentHp(msg.getCurrentHp())
                .maxHp(msg.getMaxHp())
                .currentAP(msg.getCurrentAp())
                .maxAP(msg.getMaxAp())
                .defense(msg.getDefense())
                .type(msg.getType())
                .teamId(msg.getTeamId())
                .state(EntityState.valueOf(msg.getState()))
                .build();
    }

    private HexDto toHexDto(HexMsg msg) {
        return HexDto.builder()
                .q(msg.getQ())
                .r(msg.getR())
                .build();
    }
}
