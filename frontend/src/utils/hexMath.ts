import { type Hex } from "../types/game";

export const HEX_SIZE = 30; 

export function hexToPixel(hex: Hex): { x: number; y: number } {
    const x = HEX_SIZE * Math.sqrt(3) * (hex.q + hex.r / 2);
    const y = HEX_SIZE * 3 / 2 * hex.r;
    return { x, y };
}

export function getTileColor(type: string): string {
    switch (type) {
        case 'WALL': return '#444';
        case 'FLOOR': return '#ccc';
        case 'DOOR': return '#8B4513';
        case 'PIT': return '#000';
        case 'WATER': return '#4682B4';
        default: return '#ccc';
    }
}