package dev.mygame.game.model.map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameMap {
    private static final int ASTAR_HV_COST = 10;
    private static final int ASTAR_DIAG_COST = 14;
    private Tile[][] tiles;

    private int width;
    private int height;

    public GameMap(int width, int height) {
        this.width = width;
        this.height = height;
        tiles = new Tile[width][height];
    }

    public Tile getTile(Point point) {
        if(!isWithinBounds(point))
            return null;
        return tiles[point.getX()][point.getY()];
    }

    public boolean isPassable(Point point) {
        if(!isWithinBounds(point))
            return false;
        return tiles[point.getX()][point.getY()].isPassable();
    }

    public boolean isWithinBounds(Point point) {
        return point.getX() >= 0 && point.getY() >= 0 && point.getX() < width && point.getY() < height;
    }

    public List<Point> findPath(Point start, Point target) {
        if (!isPassable(target)) {
            return new ArrayList<>();
        }
        if (start.equals(target)) {
            return Collections.singletonList(start);
        }

        final int ASTAR_HV_COST = 10;
        final int ASTAR_DIAG_COST = 14;

        PriorityQueue<AStarNode> openSet = new PriorityQueue<>(Comparator.comparingInt(AStarNode::getFCost));
        Set<Point> closedSet = new HashSet<>();

        java.util.Map<Point, AStarNode> allNodes = new HashMap<>();

        AStarNode startNode = new AStarNode(start);
        startNode.calculateHeuristic(target);
        startNode.calculateFCost();

        openSet.add(startNode);
        allNodes.put(start, startNode);

        while (!openSet.isEmpty()) {
            AStarNode currentNode = openSet.poll();

            if (currentNode.getPoint().equals(target)) {
                return reconstructPath(currentNode);
            }

            closedSet.add(currentNode.getPoint());

            for (int x = -1; x <= 1; ++x) {
                for (int y = -1; y <= 1; ++y) {
                    if (x == 0 && y == 0) {
                        continue;
                    }
                    Point neighborPoint = new Point(currentNode.getPoint().getX() + x, currentNode.getPoint().getY() + y);

                    if (!isValid(neighborPoint) || closedSet.contains(neighborPoint)) {
                        continue;
                    }

                    int moveCost = (x == 0 || y == 0) ? ASTAR_HV_COST : ASTAR_DIAG_COST;
                    int tentativeGCost = currentNode.getGCost() + moveCost;

                    AStarNode neighborNode = allNodes.get(neighborPoint);

                    if (neighborNode == null || tentativeGCost < neighborNode.getGCost()) {
                        if (neighborNode == null) {
                            neighborNode = new AStarNode(neighborPoint);
                            allNodes.put(neighborPoint, neighborNode);
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
        }

        return new ArrayList<>();
    }

    public static int calculateStepCost(Point p1, Point p2) {
        int dx = Math.abs(p1.getX() - p2.getX());
        int dy = Math.abs(p1.getY() - p2.getY());

        if(dx > 1 || dy > 1 || dx == 0 && dy == 0) {
            throw new IllegalArgumentException("Points are not adjacent or are the same.");
        }

        if(dx == 1 && dy == 1) {
            return ASTAR_DIAG_COST;
        } else {
            return ASTAR_HV_COST;
        }
    }

    public int getDistance(Point p1, Point p2) {
        int dx = Math.abs(p1.getX() - p2.getX());
        int dy = Math.abs(p1.getY() - p2.getY());
        return Math.max(dx, dy);
    }

    private List<Point> reconstructPath(AStarNode node) {
        List<Point> path = new ArrayList<>();

        AStarNode currentNode = node;
        while(currentNode.getParent() != null) {
            path.add(currentNode.getPoint());
            currentNode = currentNode.getParent();
        }
        Collections.reverse(path);
        //path.remove(0);
        return path;
    }

    private boolean isValid(Point point) {
        return isWithinBounds(point) && isPassable(point);
    }
}
