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
    VICTORY = 'VICTORY',
    DEFEAT = 'DEFEAT',
    IN_PROGRESS = 'IN_PROGRESS',
}


// =================================================================
// Core Data Structures (Ключевые структуры данных)
// =================================================================

/**
 * Представляет координату на гексагональной сетке.
 */
export interface Hex {
    q: number;
    r: number;
}

/**
 * Описывает один тайл на карте для передачи клиенту.
 */
export interface TileDto {
    q: number;
    r: number;
    type: TileType;
}


// =================================================================
// State DTOs (DTO для описания состояний)
// =================================================================

/**
 * Описывает состояние гексагональной карты.
 */
export interface MapStateDto {
    radius: number;
    tiles: TileDto[];
    spawnPoints?: Hex[];
}

/**
 * Базовое состояние для любой сущности в игре.
 */
export interface EntityStateDto {
    id: string;
    name: string;
    position: Hex;
    currentHp: number;
    maxHp: number;
    currentAP: number;
    maxAP: number;
    attackRange: number;
    state: EntityStateType;
    type: 'PLAYER' | 'MONSTER';
    dead: boolean;
    teamId?: string;
}

/**
 * Состояние игрока (наследуется от EntityStateDto).
 */
export interface PlayerStateDto extends EntityStateDto {
    type: 'PLAYER';
    userId: string;
}

/**
 * Состояние монстра (наследуется от EntityStateDto).
 */
export interface MonsterStateDto extends EntityStateDto {
    type: 'MONSTER';
    monsterType: string;
}

/**
 * Главный DTO, описывающий полное состояние игровой сессии для клиента.
 */
export interface GameSessionStateDto {
    sessionId: string;
    yourPlayerId: string;
    mapState: MapStateDto;
    entities: (PlayerStateDto | MonsterStateDto)[];
}


// =================================================================
// WebSocket Event Payloads (Данные для событий WebSocket)
// =================================================================

export interface EntityMovedEvent {
    entityId: string;
    newPosition: Hex;
    currentAp?: number;
    pathToAnimate?: Hex[];
}

export interface EntityAttackEvent {
    attackerEntityId: string;
    targetEntityId: string;
    damageCaused: number;
    attackerCurrentAP: number;
}

export interface EntityStatsUpdatedEvent {
    targetEntityId: string;
    damageToHp: number;
    currentHp: number;
    absorbedByArmor?: number;
    currentDefense?: number;
    dead: boolean;
}

export interface PlayerJoinedEvent {
    player: PlayerStateDto;
}

export interface PlayerLeftEvent {
    entityId: string;
}

export interface ErrorEvent {
    message: string;
    errorCode?: string;
}

export interface CombatStartedEvent {
    combatId: string;
    combatInitiatorId: string;
    teams: { teamId: string; memberIds: string[] }[];
    initialTurnOrder: string[];
    combatants: EntityStateDto[];
}

export interface CombatNextTurnEvent {
    combatId: string;
    currentTurnEntityId: string;
    currentAp: number;
}

export interface TeamInviteEvent {
    fromPlayerId: string;
    fromPlayerName: string;
    toTeamId: string;
}

export interface TeamUpdatedEvent {
    teamId: string;
    memberIds: string[];
}

// =================================================================
// Client Actions (Действия, отправляемые клиентом на сервер)
// =================================================================

export interface PlayerAction {
    actionType: 'MOVE' | 'ATTACK' | 'END_TURN'; // Упрощаем для начала
    targetId?: string;
    targetHex?: Hex;
}


// =================================================================
// Generic Wrapper (Общая обертка для всех обновлений)
// =================================================================

export interface GameUpdatePayload<T> {
    actionType: string; 
    payload: T;
}

export interface CombatEndedEvent {
    combatId: string;
    outcome: CombatOutcome;
    winningTeamId?: string;
}