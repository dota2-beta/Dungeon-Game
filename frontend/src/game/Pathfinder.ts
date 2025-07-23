import type { EntityStateDto, Hex, MapStateDto } from '../types/dto';

// --- Хелперы, специфичные для этого файла ---
const hexAdd = (a: Hex, b: Hex): Hex => ({ q: a.q + b.q, r: a.r + b.r });
const HEX_DIRECTIONS = [
    { q: 1, r: 0 }, { q: 1, r: -1 }, { q: 0, r: -1 },
    { q: -1, r: 0 }, { q: -1, r: 1 }, { q: 0, r: 1 }
];

export class Pathfinder {
    // Хранит "чистую" карту проходимости (только стены/пол)
    private baseWalkableTiles: Set<string>;

    constructor(mapState: MapStateDto) {
        this.baseWalkableTiles = new Set();
        mapState.tiles.forEach(t => {
            if (t.type === 'FLOOR') {
                this.baseWalkableTiles.add(`${t.q},${t.r}`);
            }
        });
    }

    /**
     * Находит кратчайший путь с помощью Поиска в ширину (BFS).
     */
    public findPath(startHex: Hex, endHex: Hex, entities: EntityStateDto[]): Hex[] {
        const startKey = `${startHex.q},${startHex.r}`;
        const endKey = `${endHex.q},${endHex.r}`;

        const currentWalkable = new Set(this.baseWalkableTiles);
        entities.forEach(e => {
            if (!e.dead) {
                currentWalkable.delete(`${e.position.q},${e.position.r}`);
            }
        });
        
        currentWalkable.add(startKey);
        currentWalkable.add(endKey);

        if (!this.baseWalkableTiles.has(endKey)) {
            return [];
        }

        const queue: Hex[] = [startHex];
        const cameFrom: Map<string, Hex | null> = new Map();
        cameFrom.set(startKey, null);

        while (queue.length > 0) {
            const current = queue.shift()!;
            
            if (current.q === endHex.q && current.r === endHex.r) {
                const path: Hex[] = [];
                let temp: Hex | null = current;
                while (temp) {
                    path.push(temp);
                    const keyForMap: string = `${temp.q},${temp.r}`;
                    temp = cameFrom.get(keyForMap) ?? null;
                }
                return path.reverse();
            }

            for (const direction of HEX_DIRECTIONS) {
                const neighbor = hexAdd(current, direction);
                const neighborKey = `${neighbor.q},${neighbor.r}`;

                if (currentWalkable.has(neighborKey) && !cameFrom.has(neighborKey)) {
                    queue.push(neighbor);
                    cameFrom.set(neighborKey, current);
                }
            }
        }

        return [];
    }
}