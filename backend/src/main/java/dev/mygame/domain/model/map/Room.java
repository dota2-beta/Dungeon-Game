package dev.mygame.domain.model.map;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Room {
    private final int width;
    private final int height;
    private final Point topLeft;

    public Point getCenter() {
        return new Point(topLeft.getX() + width / 2, topLeft.getY() + height / 2);
    }

    public boolean intersects(Room other) {
        // this.topLeft.x - левый край текущей комнаты
        // this.topLeft.x + this.width - правый край текущей комнаты (не включая)
        // other.topLeft.x - левый край другой комнаты
        // other.topLeft.x + other.width - правый край другой комнаты (не включая)

        return ( this.topLeft.getX() < other.topLeft.getX() + other.getWidth()
               && this.topLeft.getX() + this.width > other.topLeft.getX()
               && this.topLeft.getY() < other.topLeft.getY() + other.getHeight()
               && this.topLeft.getY() + this.height > other.topLeft.getY()
               );
    }
}
