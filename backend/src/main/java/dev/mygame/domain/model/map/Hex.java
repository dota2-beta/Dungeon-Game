package dev.mygame.domain.model.map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Hex {
    private int q;
    private int r;

    public static final List<Hex> DIRECTIONS = List.of(
            new Hex(1, 0),
            new Hex(1, -1),
            new Hex(0, -1),
            new Hex(-1, 0),
            new Hex(-1, 1),
            new Hex(0, 1)
    );

    /**
     * Возвращает соседа в заданном направлении
     * @param direction Индекс направления от 0 до 5
     * @return Координата соседа
     */
    public Hex getNeighbor(int direction) {
        return this.add(DIRECTIONS.get(direction));
    }

    public int getS() {
        return -q-r;
    }

    public Hex add(Hex h) {
        return new Hex(this.q + h.getQ(), this.r + h.getR());
    }

    public Hex subtract(Hex h) {
        return new Hex(this.q - h.getQ(), this.r - h.getR());
    }

    public int distanceTo(Hex h) {
        Hex vec = this.subtract(h);
        return (Math.abs(vec.getQ()) + Math.abs(vec.getR()) + Math.abs(vec.getS())) / 2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hex hex = (Hex) o;
        return q == hex.q && r == hex.r;
    }

    @Override
    public int hashCode() {
        return Objects.hash(q, r);
    }

    @Override
    public String toString() {
        return "Hex(" + "q=" + q + ", r=" + r + ')';
    }

}
