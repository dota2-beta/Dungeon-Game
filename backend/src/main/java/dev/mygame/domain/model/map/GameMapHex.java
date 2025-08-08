package dev.mygame.domain.model.map;

import dev.mygame.domain.model.Entity;
import dev.mygame.service.internal.SpawnPointInfo;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class GameMapHex {
    private final Map<Hex, Tile> tiles;
    private final List<Hex> playerSpawnPoints;
    private final List<SpawnPointInfo> monsterSpawnPoints;

    private int nextPlayerSpawnIndex = 0;

    @Builder
    public GameMapHex(Map<Hex, Tile> tiles, List<Hex> playerSpawnPoints, List<SpawnPointInfo> monsterSpawnPoints) {
        this.tiles = (tiles != null) ? tiles : new HashMap<>();
        this.playerSpawnPoints = (playerSpawnPoints != null) ? playerSpawnPoints : new ArrayList<>();
        this.monsterSpawnPoints = (monsterSpawnPoints != null) ? monsterSpawnPoints : new ArrayList<>();
    }


    public Tile getTile(Hex hex) {
        return this.tiles.get(hex);
    }

    public void setTile(Hex hex, Tile tile) {
        this.tiles.put(hex, tile);
    }

    public Collection<Tile> getAllTiles() {
        return this.tiles.values();
    }

    public Set<Map.Entry<Hex, Tile>> getTileEntries() {
        return tiles.entrySet();
    }

    public List<Hex> findPath(Hex start, Hex end, Collection<Entity> otherEntities, String startEntityId) {
        Tile startTile = getTile(start);
        Tile targetTile = getTile(end);

        if (startTile == null) {
            return new ArrayList<>();
        }
        if (targetTile == null) {
            return new ArrayList<>();
        }
        if (!targetTile.isPassable()) {
            return new ArrayList<>();
        }
        if (targetTile == null || !targetTile.isPassable()) {
            return new ArrayList<>();
        }
        if (start.equals(end)) {
            return Collections.singletonList(start);
        }

        Set<Hex> occupiedHexes = otherEntities.stream()
                .filter(e -> !e.getId().equals(startEntityId))
                .map(Entity::getPosition)
                .collect(Collectors.toSet());

        PriorityQueue<AStarNode> openSet = new PriorityQueue<>(Comparator.comparingInt(AStarNode::getFCost));
        Map<Hex, AStarNode> allNodes = new HashMap<>();

        AStarNode startNode = new AStarNode(start, null, 0, start.distanceTo(end));
        openSet.add(startNode);
        allNodes.put(start, startNode);

        while (!openSet.isEmpty()) {
            AStarNode currentNode = openSet.poll();

            if (currentNode.getHex().equals(end)) {
                return reconstructPath(currentNode);
            }
            if (currentNode.getGCost() > allNodes.get(currentNode.getHex()).getGCost()) {
                continue;
            }

            for (Hex direction : Hex.DIRECTIONS) {
                Hex neighborHex = currentNode.getHex().add(direction);

                Tile neighborTile = getTile(neighborHex);
                if (neighborTile == null || !neighborTile.isPassable()) {
                    continue;
                }
                if (occupiedHexes.contains(neighborHex) && !neighborHex.equals(end)) {
                    continue;
                }

                int newGCost = currentNode.getGCost() + 1;
                AStarNode neighborNode = allNodes.get(neighborHex);
                if (neighborNode == null || newGCost < neighborNode.getGCost()) {
                    AStarNode newNode = new AStarNode(neighborHex, currentNode, newGCost, neighborHex.distanceTo(end));
                    allNodes.put(neighborHex, newNode);
                    openSet.add(newNode);
                }
            }
        }
        return new ArrayList<>();
    }
    private List<Hex> reconstructPath(AStarNode node) {
        List<Hex> path = new ArrayList<>();

        AStarNode currentNode = node;
        while(currentNode != null) {
            path.add(currentNode.getHex());
            currentNode = currentNode.getParent();
        }
        Collections.reverse(path);
        //path.remove(0);
        return path;
    }

    /**
     * Возвращает следующую доступную точку спавна для игрока.
     * Если точки кончились, начинает сначала (циклически).
     * @return Координата для спавна.
     */
    public Hex getAvailablePlayerSpawnPoint() {
        if (playerSpawnPoints == null || playerSpawnPoints.isEmpty()) {
            // если на карте нет точек 'P'
            throw new IllegalStateException("No player spawn points defined on the map!");
            // return new Hex(0, 0);
        }

        Hex spawnPoint = playerSpawnPoints.get(nextPlayerSpawnIndex);
        nextPlayerSpawnIndex = (nextPlayerSpawnIndex + 1) % playerSpawnPoints.size();
        return spawnPoint;
    }
}
