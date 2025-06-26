package dev.mygame.game.model.map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@AllArgsConstructor
public class AStarNode {
    private Point point;
    private AStarNode parent;
    private int gCost;
    private int hCost;
    private int fCost;

    public AStarNode(Point point) {
        this.point = point;
        this.parent = null;
        this.gCost = 0;
        this.hCost = 0;
        this.fCost = 0;
    }

    public AStarNode(Point point, AStarNode parent, int gCost) {
        this.point = point;
        this.parent = parent;
        this.gCost = gCost;
        this.hCost = 0;
        this.fCost = 0;
    }

    public void calculateHeuristic(Point finalPoint) {
        this.hCost = Math.max(Math.abs(finalPoint.getX() - this.getPoint().getX()),
                Math.abs(finalPoint.getY() - this.getPoint().getY())) * 10;
    }

    public void calculateFCost() {
        this.fCost = this.gCost + this.hCost;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AStarNode aStarNode = (AStarNode) o;
        return Objects.equals(point, aStarNode.point);
    }

    @Override
    public int hashCode() {
        return Objects.hash(point);
    }
}
