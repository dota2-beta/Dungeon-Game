package com.abs.dungeoncrawler.gamesessionservice.mapper;

import com.abs.dungeoncrawler.gamesessionservice.domain.GameSession;
import com.abs.dungeoncrawler.gamesessionservice.domain.model.Entity;
import com.abs.dungeoncrawler.gamesessionservice.domain.model.Player;
import com.abs.dungeoncrawler.gamesessionservice.domain.model.map.GameMapHex;
import com.abs.dungeoncrawler.gamesessionservice.domain.model.map.Hex;
import com.abs.dungeoncrawler.gamesessionservice.domain.model.map.Tile;
import com.dungeoncrawler.contracts.grpc.common.HexMsg;
import com.dungeoncrawler.contracts.grpc.common.TileMsg;
import com.dungeoncrawler.contracts.grpc.gamesession.EntityStateMsg;
import com.dungeoncrawler.contracts.grpc.gamesession.GameSessionStateMsg;
import com.dungeoncrawler.contracts.grpc.gamesession.MapStateMsg;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GameSessionMapper {
    public GameSessionStateMsg toProto(GameSession session) {
        return GameSessionStateMsg.newBuilder()
                .setSessionId(session.getSessionId())
                .setMapState(toMapStateProto(session.getGameMap()))
                .addAllEntities(session.getEntities().values().stream()
                        .map(this::toEntityStateProto)
                        .toList())
                .build();
    }

    private MapStateMsg toMapStateProto(GameMapHex map) {
        List<TileMsg> tileMsgs = map.getTiles().entrySet().stream()
                .map(entry -> {
                    Hex hex = entry.getKey();
                    Tile tile = entry.getValue();

                    return TileMsg.newBuilder()
                            .setCoordinate(toHexMsg(hex))
                            .setType(tile.getType().name())
                            .build();
                })
                .toList();

        return MapStateMsg.newBuilder()
                .addAllTiles(tileMsgs)
                .build();
    }

    private EntityStateMsg toEntityStateProto(Entity entity) {
        var builder =  EntityStateMsg.newBuilder()
                .setId(entity.getId())
                .setName(entity.getName())
                .setPosition(toHexMsg(entity.getPosition()))
                .setCurrentHp(entity.getCurrentHp())
                .setMaxHp(entity.getMaxHp())
                .setCurrentAp(entity.getCurrentAP())
                .setMaxAp(entity.getMaxAP())
                .setDefense(entity.getDefense())
                .setType(entity instanceof Player ? "PLAYER" : "MONSTER")
                .setTeamId(entity.getTeamId())
                .setState(entity.getState().name());
        if (entity instanceof Player player) {
            builder.setUserId(player.getUserId());
        }
        return builder.build();
    }

    private HexMsg toHexMsg(Hex hex) {
        return HexMsg.newBuilder()
                .setQ(hex.getQ())
                .setR(hex.getR())
                .build();
    }
}
