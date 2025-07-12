package dev.mygame.service;

import dev.mygame.config.MapGenerationProperties;
import dev.mygame.domain.model.map.Point;
import dev.mygame.domain.model.map.Room;
import dev.mygame.enums.TileType;
import dev.mygame.domain.model.map.GameMap;
import dev.mygame.domain.model.map.Tile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class MapGenerator {
    private final Random random = new Random();
    private final MapGenerationProperties props;

    public MapGenerator(MapGenerationProperties props) {
        this.props = props;
    }

    public GameMap generateCleanMap() {
        int width = this.props.getDefaultWidth();
        int height = this.props.getDefaultHeight();

        GameMap gameMap = new GameMap(width,height);

        Tile[][] tiles = new Tile[width][height];
        for(int i = 0; i < width; ++ i)
            for(int j = 0; j < height; ++ j)
            {
                //TileType type = (i == 0 || i == width - 1 || j == 0 || j == height - 1) ? TileType.WALL : TileType.FLOOR;
                TileType type = TileType.WALL;
                tiles[i][j] = new Tile(type, null);
            }
        gameMap.setTiles(tiles);
        return gameMap;
    }

    public GameMap generateDungeon() {
        int width = this.props.getDefaultWidth();
        int height = this.props.getDefaultHeight();
        int minRooms = this.props.getMinRooms();
        int maxRooms = this.props.getMaxRooms();
        int minRoomSize = this.props.getMinRoomSize();
        int maxRoomSize = this.props.getMaxRoomSize();

        GameMap gameMap = new GameMap(width,height);
        gameMap.fill(TileType.WALL);

        //int roomCount = minRooms + random.nextInt(maxRooms - minRooms + 1);
        List<Room> rooms = new ArrayList<>();


        for (int i = 0; i < maxRooms; i++) {
            int w = random.nextInt(maxRoomSize - minRoomSize + 1) + minRoomSize;
            int h = random.nextInt(maxRoomSize - minRoomSize + 1) + minRoomSize;
            int x = random.nextInt(width - w - 1) + 1;
            int y = random.nextInt(height - h - 1) + 1;

            Room newRoom = new Room(w, h, new Point(x,y));

            boolean intersects = false;
            for(Room otherRoom : rooms)
                if(newRoom.intersects(otherRoom)) {
                    intersects = true;
                    break;
                }
            if(!intersects)
            {
                createRoomOnTheMap(gameMap, newRoom);
                rooms.add(newRoom);
            }
        }

        for (int i = 1; i < rooms.size(); i++) {
            Point prevCenter = rooms.get(i - 1).getCenter();
            Point newCenter = rooms.get(i).getCenter();

            if (random.nextBoolean()) {
                createHorizontalTunnel(gameMap, prevCenter.getX(), newCenter.getX(), prevCenter.getY());
                createVerticalTunnel(gameMap, prevCenter.getY(), newCenter.getY(), newCenter.getX());
            } else {
                createVerticalTunnel(gameMap, prevCenter.getY(), newCenter.getY(), prevCenter.getX());
                createHorizontalTunnel(gameMap, prevCenter.getX(), newCenter.getX(), newCenter.getY());
            }
        }

        if (!rooms.isEmpty()) {
            gameMap.setSpawnPoint(rooms.get(0).getCenter());
        } else {
            System.err.println("Map generation failed: no rooms were created.");
            gameMap.setSpawnPoint(new Point(1, 1));
        }

        return gameMap;
    }

    private void createRoomOnTheMap(GameMap gameMap, Room room) {
        int startX = room.getTopLeft().getX();
        int startY = room.getTopLeft().getY();
        int endX = startX + room.getWidth();
        int endY = startY + room.getHeight();

        for(int i = startX; i < endX; ++i)
            for(int j = startY; j < endY; ++j)
                gameMap.setTile(new Point(i, j), TileType.FLOOR);

    }

    private void createHorizontalTunnel(GameMap map, int x1, int x2, int y) {
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
            map.setTile(new Point(x, y), TileType.FLOOR);
        }
    }

    private void createVerticalTunnel(GameMap map, int y1, int y2, int x) {
        for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
            map.setTile(new Point(x, y), TileType.FLOOR);
        }
    }
}
