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

    public boolean intersects(Room room) {
        if( this.topLeft.getX() + width < room.getTopLeft().getX()
          || this.topLeft.getX() > room.getTopLeft().getX() + room.getWidth() ) {
            return false;
        }
        if( this.topLeft.getY() + height < room.getTopLeft().getY()
            || this.topLeft.getY() > room.getTopLeft().getY() + room.getHeight() ) {
            return false;
        }
        return true;
    }
}
