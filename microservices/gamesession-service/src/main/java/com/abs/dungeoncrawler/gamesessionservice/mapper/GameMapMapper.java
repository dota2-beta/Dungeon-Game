package com.abs.dungeoncrawler.gamesessionservice.mapper;

import com.abs.dungeonCrawler.eventcontracts.enums.TileType;
import com.abs.dungeoncrawler.gamesessionservice.domain.model.map.*;
import com.dungeoncrawler.contracts.grpc.common.HexMsg;
import com.dungeoncrawler.contracts.grpc.gamedata.GameMapResponse;
import com.dungeoncrawler.contracts.grpc.gamedata.SpawnPointInfoMsg;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Component
public class GameMapMapper {
    public GameMapHex grpcGameMapToGameMapHex(GameMapResponse proto) {
        Map<Hex, Tile> tiles = proto.getTilesList().stream()
                .collect(toMap(
                        t -> grpcHexMsgToHex(t.getCoordinate()),
                        t -> Tile.builder()
                                        .type(getTileTypeFromChar(t.getType().charAt(0)))
                                        .occupiedById(null)
                                        .build()
                ));
        return GameMapHex.builder()
                .tiles(tiles)
                .playerSpawnPoints(listGrpcSpawnPointToListSpawnPointInfo(proto.getPlayerSpawnPointsList()))
                .monsterSpawnPoints(listGrpcSpawnPointToListSpawnPointInfo(proto.getMonsterSpawnPointsList()))
                .build();
    }

    public HexMsg grpcHexToHexMsg(Hex hex) {
        return HexMsg.newBuilder()
                .setQ(hex.getQ())
                .setR(hex.getR())
                .build();
    }

    public Hex grpcHexMsgToHex(HexMsg msg) {
        return Hex.builder()
                .q(msg.getQ())
                .r(msg.getR())
                .build();
    }

    private TileType getTileTypeFromChar(char symbol) {
        return switch (symbol) {
            case 'W' -> TileType.WALL;
            case '.' -> TileType.FLOOR;
            case 'D' -> TileType.DOOR;
            default -> throw new IllegalArgumentException("Unknown map symbol: " + symbol);
        };
    }

    private SpawnPointInfo grpcSpawnPointToSpawnPointInfo(SpawnPointInfoMsg msg) {
        String templateId = msg.getTemplateId();
        if (!templateId.isEmpty()) {
            return new SpawnPointInfo(grpcHexMsgToHex(msg.getCoordinate()), templateId);
        } else
            return new SpawnPointInfo(grpcHexMsgToHex(msg.getCoordinate()), null);
    }

    private List<SpawnPointInfo> listGrpcSpawnPointToListSpawnPointInfo(List<SpawnPointInfoMsg> proto) {
        return proto.stream()
                .map(this::grpcSpawnPointToSpawnPointInfo)
                .toList();
    }
}
