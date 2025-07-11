// =================================================================
// Enums (Перечисления)
// =================================================================

export enum EntityStateType {
    EXPLORING = 'EXPLORING',
    COMBAT = 'COMBAT',
}

export enum TileType {
    FLOOR = 'FLOOR',
    WALL = 'WALL',
    PIT = 'PIT',
    DOOR = 'DOOR',
    WATER = 'WATER',
}

export enum CombatOutcome {
    IN_PROGRESS = 'IN_PROGRESS',
    PLAYERS_WIN = 'PLAYERS_WON',
    MONSTERS_WIN = 'MONSTERS_WON',
    DRAW = 'DRAW',
}

// =================================================================
// Base & State DTOs (Базовые DTO и DTO состояний)
// =================================================================

export interface Point {
    x: number;
    y: number;
}

export interface MapState {
    width: number;
    height: number;
    tiles: TileType[];
    spawnPoints?: Point[];
}

export interface EntityState {
    id: string;
    name: string;
    position: Point;
    currentHp: number;
    maxHp: number;
    state: EntityStateType;
    type: 'PLAYER' | 'MONSTER';
    isDead: boolean;
}

export interface PlayerState extends EntityState {
    type: 'PLAYER';
    userId: string;
}

export interface MonsterState extends EntityState {
    type: 'MONSTER';
    monsterType: string;
}

export interface GameSessionState {
    sessionId: string;
    yourPlayerId: string;
    mapState: MapState;
    entities: (PlayerState | MonsterState)[];
}


// =================================================================
// WebSocket Event Payloads (Данные для событий WebSocket)
// =================================================================

export interface EntityMovedEvent {
    entityId: string;
    newPosition: Point;
    remainingAp?: number;
    pathToAnimate?: Point[]; 
    reachedTarget?: boolean;
}

export interface EntityAttackEvent {
    attackerEntityId: string;
    targetEntityId: string;
    damageCaused: number;
}

export interface EntityStatsUpdatedEvent {
    targetEntityId: string;
    damageToHp: number;
    currentHp: number;
    absorbedByArmor?: number;
    newDefense?: number;
    dead?: boolean;
}

export interface CombatStartedEvent {
    combatId: string;
    combatInitiatorId: string;
    team1EntityIds: string[]; // на бэке Set
    team2EntityIds: string[];
    participantIds: string[];
}

export interface CombatEndedEvent {
    combatId: string;
    outcome: CombatOutcome;
}

export interface ErrorEvent {
    message: string;
    errorCode?: string;
}


// =================================================================
// Payloads для событий, которые мы еще не стандартизировали
// (можно будет обновить в будущем)
// =================================================================

export interface PlayerJoinedPayload {
    player: PlayerState;
}

export interface PlayerLeftPayload {
    entityId: string;
}

export interface TurnStartedPayload {
    combatId: string;
    currentTurnEntityId: string;
    currentAp: number;
}


// =================================================================
// Вспомогательные типы
// =================================================================

export interface PlayerAction {
    actionType: 'MOVE' | 'ATTACK' | 'INTERACT' | 'END_TURN';
    targetId?: string;
    targetPoint?: Point;
    itemId?: string;
}

export interface GameUpdatePayload<T> {
    actionType: string; 
    payload: T;
}