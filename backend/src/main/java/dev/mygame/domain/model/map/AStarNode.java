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

    /**
     * Основной конструктор для создания узла.
     * Сразу вычисляет fCost.
     *
     * @param hex      Координата гекса.
     * @param parent   Предыдущий узел в пути.
     * @param gCost    Стоимость пути от старта до этого узла.
     * @param hCost    Эвристическая оценка расстояния до цели.
     */
    public AStarNode(Hex hex, AStarNode parent, int gCost, int hCost) {
        this.hex = hex;
        this.parent = parent;
        this.gCost = gCost;
        this.hCost = hCost;
        this.fCost = gCost + hCost; // fCost вычисляется сразу
    }

    /**
     * Вычисляет эвристику (hCost).
     * Для гексов эвристика - это просто гексагональное расстояние.
     * @param finalHex Целевой гекс.
     */
    public void calculateHeuristic(Hex finalHex) {
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
