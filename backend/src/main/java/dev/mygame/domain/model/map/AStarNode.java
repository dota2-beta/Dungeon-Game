package dev.mygame.domain.model.map;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Objects;

@Data
@AllArgsConstructor
public class AStarNode {
    private Hex hex;
    private AStarNode parent;
    private int gCost;
    private int hCost;
    private int fCost;

    public AStarNode(Hex hex) {
        this.hex = hex;
        this.parent = null;
        this.gCost = 0;
        this.hCost = 0;
        this.fCost = 0;
    }

    /**
     * Вычисляет эвристику (hCost).
     * Для гексов эвристика - это просто гексагональное расстояние.
     * @param finalHex Целевой гекс.
     */
    public void calculateHeuristic(Hex finalHex) {
        // Используем наш метод distanceTo. Умножаем на 10, чтобы работать с целыми числами,
        // так как стоимость шага у нас тоже будет 10.
        this.hCost = this.hex.distanceTo(finalHex) * 10;
    }

    public void calculateFCost() {
        this.fCost = this.gCost + this.hCost;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AStarNode aStarNode = (AStarNode) o;
        return Objects.equals(hex, aStarNode.hex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hex);
    }
}
