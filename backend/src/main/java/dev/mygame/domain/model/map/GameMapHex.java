package dev.mygame.domain.model.map;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.*;

@Getter
public class GameMapHex {
    private final Map<Hex, Tile> tiles;
    private final List<Hex> playerSpawnPoints;
    private final List<Hex> monsterSpawnPoints;

    private int nextPlayerSpawnIndex = 0;

    @Builder
    public GameMapHex(Map<Hex, Tile> tiles, List<Hex> playerSpawnPoints, List<Hex> monsterSpawnPoints) {
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

    public List<Hex> findPath(Hex start, Hex target) {
        Tile targetTile = getTile(target);
        if (targetTile == null || !targetTile.isPassable()) {
            return new ArrayList<>();
        }
        if (start.equals(target)) {
            return Collections.singletonList(start);
        }

        final int MOVE_COST = 10;

        PriorityQueue<AStarNode> openSet = new PriorityQueue<>(Comparator.comparingInt(AStarNode::getFCost));
        Set<Hex> closedSet = new HashSet<>();
        Map<Hex, AStarNode> allNodes = new HashMap<>();

        AStarNode startNode = new AStarNode(start);
        startNode.calculateHeuristic(target);
        startNode.calculateFCost();

        openSet.add(startNode);
        allNodes.put(start, startNode);

        while (!openSet.isEmpty()) {
            AStarNode currentNode = openSet.poll();

            if (currentNode.getHex().equals(target)) {
                return reconstructPath(currentNode);
            }

            closedSet.add(currentNode.getHex());

            for (Hex direction : Hex.DIRECTIONS) {
                Hex neighborHex = currentNode.getHex().add(direction);

                if (closedSet.contains(neighborHex)) {
                    continue;
                }

                Tile neighborTile = getTile(neighborHex);
                if (neighborTile == null) {
                    continue;
                }
                if (!neighborTile.isPassable()) {
                    continue;
                }

                int tentativeGCost = currentNode.getGCost() + MOVE_COST;

                AStarNode neighborNode = allNodes.get(neighborHex);

                if (neighborNode == null || tentativeGCost < neighborNode.getGCost()) {
                    if (neighborNode == null) {
                        neighborNode = new AStarNode(neighborHex);
                        allNodes.put(neighborHex, neighborNode);
                    }

                    neighborNode.setParent(currentNode);
                    neighborNode.setGCost(tentativeGCost);
                    neighborNode.calculateHeuristic(target);
                    neighborNode.calculateFCost();

                    if (openSet.contains(neighborNode)) {
                        openSet.remove(neighborNode);
                    }
                    openSet.add(neighborNode);
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
