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
    state: EntityStateType;
    type: 'PLAYER' | 'MONSTER';
    isDead: boolean;
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

/**
 * Событие: сущность переместилась.
 */
export interface EntityMovedEvent {
    entityId: string;
    newPosition: Hex;
    remainingAp?: number;
    pathToAnimate?: Hex[];
    reachedTarget?: boolean;
}

/**
 * Событие: одна сущность атаковала другую.
 */
export interface EntityAttackEvent {
    attackerEntityId: string;
    targetEntityId: string;
    damageCaused: number; 
}

/**
 * Событие: у сущности изменились статы (обычно в результате урона).
 */
export interface EntityStatsUpdatedEvent {
    targetEntityId: string;
    damageToHp: number;
    currentHp: number;
    absorbedByArmor?: number;
    currentDefense?: number;
    isDead: boolean;
}

/**
 * Событие: сущность покинула игру.
 */
export interface PlayerLeftEvent {
    entityId: string;
}

/**
 * Событие: отправка ошибки конкретному игроку.
 */
export interface ErrorEvent {
    message: string;
    errorCode?: string;
}


// =================================================================
// Client Actions (Действия, отправляемые клиентом на сервер)
// =================================================================

/**
 * Описывает действие, которое игрок хочет совершить.
 */
export interface PlayerAction {
    actionType: 'MOVE' | 'ATTACK' | 'INTERACT' | 'END_TURN';
    targetId?: string;
    targetHex?: Hex;
    itemId?: string;
}


// =================================================================
// Generic Wrapper (Общая обертка для всех обновлений)
// =================================================================

/**
 * Обертка для всех сообщений, приходящих по топику /game-updates.
 */
export interface GameUpdatePayload<T> {
    actionType: string; 
    payload: T;
}