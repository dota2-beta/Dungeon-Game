export enum EntityState {
    EXPLORING = 'EXPLORING',
    COMBAT = 'COMBAT',
}

export enum TileType {
    FLOOR = 'FLOOR',
    WALL = 'WALL',
    PIT = 'PIT',
    DOOR = 'DOOR',
    WATER = 'WATER'
}

// Типы для координат
export interface Point {
    x: number;
    y: number;
}

// DTO для карты
export interface MapClientState {
    width: number;
    height: number;
    tiles: TileType[];
    spawnPoints?: Point[];
}

// Абстрактный/общий DTO для сущностей
export interface EntityClientState {
    id: string;
    name: string;
    position: Point;
    currentHp: number;
    maxHp: number;
    state: EntityState;
    type: 'PLAYER' | 'MONSTER';
    isDead: boolean
}

// DTO для игрока
export interface PlayerClientState extends EntityClientState {
    type: 'PLAYER';
    userId: string;
}

// DTO для монстра
export interface MonsterClientState extends EntityClientState {
    type: 'MONSTER';
    monsterType: string;
}

// DTO для объекта
export interface GameObjectClientState {
    id: string;
    type: string;
    position: Point;
    state: string;
}

// DTO для состояния боя (пока можно оставить пустым)
export interface CombatStatePayload {
    combatId: string;
    participantIds: string[];
    turnOrder: string[];
    currentTurnEntityId: string;
}

// Главный DTO состояния
export interface GameSessionState {
    sessionId: string;
    yourPlayerId: string;
    mapState: MapClientState;
    entities: (PlayerClientState | MonsterClientState)[]; // Массив, содержащий и игроков, и монстров
    // gameObjects: GameObjectClientState[];
    // activeCombats: CombatStatePayload[];
}

// Payload для события 'entity_moved'
export interface EntityMovedPayload {
    entityId: string;
    newPosition: Point;
    pathToAnimate?: Point[]; 
    remainingAp?: number;
}

// Payload для события 'damage_taken' или 'entity_stats_updated'
export interface DamageTakenPayload {
    targetId: string;
    damageToHp: number;
    newHp: number;
    absorbedByArmor?: number;
    newDefense?: number;
}

// Enum для исхода боя (должен соответствовать Java Enum)
export enum CombatOutcome {
    IN_PROGRESS = 'IN_PROGRESS',
    PLAYERS_WIN = 'PLAYERS_WON',
    MONSTERS_WIN = 'MONSTERS_WON',
    DRAW = 'DRAW',
}

// Payload для события 'combat_ended'
export interface CombatEndedPayload {
    combatId: string;
    outcome: CombatOutcome; 
}

// Payload для события 'turn_started'
export interface TurnStartedPayload {
    combatId: string;
    currentTurnEntityId: string;
    currentAp: number;
}

// DTO для игрового действия
export interface PlayerAction {
    actionType: 'MOVE' | 'ATTACK' | 'INTERACT' | 'END_TURN';
    targetId?: string;
    targetPoint?: Point,
    itemId?: string;
}

export interface GameUpdatePayload<T> {
    actionType: string; 
    payload: T;
}

export interface PlayerJoinedPayload {
    player: PlayerClientState;
}

export interface PlayerLeftPayload {
    entityId: string;
}