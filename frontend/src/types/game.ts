// src/types/game.ts

// ==========================================
// 🧩 Primitives & Enums (Replacement)
// ==========================================

export interface Hex {
    q: number;
    r: number;
}

// 1. EntityState
export const EntityState = {
    EXPLORING: "EXPLORING",
    COMBAT: "COMBAT"
} as const;

// Создаем тип, который принимает только значения "EXPLORING" | "COMBAT"
export type EntityState = typeof EntityState[keyof typeof EntityState];


// 2. EntityType
export const EntityType = {
    PLAYER: "PLAYER",
    MONSTER: "MONSTER"
} as const;

export type EntityType = typeof EntityType[keyof typeof EntityType];


// 3. CombatOutcome
export const CombatOutcome = {
    VICTORY: "VICTORY",
    DEFEAT: "DEFEAT",
    DRAW: "DRAW",
    SOME_TEAM_WON: "SOME_TEAM_WON",
    IN_PROGRESS: "IN_PROGRESS",
    END_BY_AGREEMENT: "END_BY_AGREEMENT",
    MERGED: "MERGED"
} as const;

export type CombatOutcome = typeof CombatOutcome[keyof typeof CombatOutcome];

// ==========================================
// 👤 Core Models (DTOs)
// ==========================================

export interface EntityStateDto {
    id: string;
    name: string;
    position: Hex;
    currentHp: number;
    maxHp: number;
    currentAP: number;
    maxAP: number;
    defense: number;
    type: EntityType | string; // PLAYER or MONSTER
    teamId: string;
    state: EntityState;
    userId?: string;
}

export interface CombatTeamDto {
    teamId: string;
    memberIds: string[]; // List<String> с бэкенда приходит как массив строк
}

// ==========================================
// 📨 Event Payloads (То, что лежит внутри "payload")
// ==========================================

// --- Session Management ---
export interface SessionCreatedEvent {
    sessionId: string;
    userId: string;
}

export interface EntityJoinedEvent {
    sessionId: string;
    entityStateDto: EntityStateDto;
}

export interface EntityMovedEvent {
    sessionId: string;
    entityId: string;
    newPosition: Hex;
    currentAP: number; 
}

export interface EntityStatsUpdatedEvent {
    sessionId: string;
    entityId: string;
    // Бэкенд шлет именно эти поля:
    currentHp: number;      
    currentDefense: number; 
    isDead: boolean;
    damageToHp: number;
    absorbedByArmor: number;
}

export interface EntityAttackEvent {
    sessionId: string;
    attackerEntityId: string;
    targetEntityId: string;
    damageCaused: number;
    attackerCurrentAP: number;
}

export interface EntityDiedEvent {
    sessionId: string;
    entityId: string;
}

// --- Combat System ---
export interface CombatStartedEvent {
    sessionId: string;
    combatId: string;
    combatInitiatorId?: string;
    teams: CombatTeamDto[];
    initialTurnOrder: string[]; // Список ID сущностей
    combatants: EntityStateDto[];
}

export interface CombatParticipantsJoinedEvent {
    sessionId: string;
    combatId: string;
    participants: EntityStateDto[]; // Новые участники
    turnOrder: string[]; // Обновленная очередь
    activeEntityId: string;
}

export interface CombatTurnChangedEvent {
    sessionId: string;
    combatId: string;
    activeEntityId: string; // Кто теперь ходит
    currentAP: number;
    isMonster?: boolean; // Полезно для UI (блокировать кнопки)
}

export interface CombatEndedEvent {
    sessionId: string;
    combatId: string;
    outcome: CombatOutcome;
    winningTeamId?: string;
}

// ==========================================
// 📦 WebSocket Wrapper (Конверт сообщения)
// ==========================================

// Тип для дискриминированного объединения (Discriminated Union)
// Это позволит TypeScript автоматически понимать тип payload при проверке eventType
export type GameEvent =
    | { eventType: 'session_created'; payload: SessionCreatedEvent }
    | { eventType: 'entity_joined'; payload: EntityJoinedEvent }
    | { eventType: 'entity_moved'; payload: EntityMovedEvent }
    | { eventType: 'entity_stats_updated'; payload: EntityStatsUpdatedEvent }
    | { eventType: 'entity_attack'; payload: EntityAttackEvent }
    | { eventType: 'entity_died'; payload: EntityDiedEvent }
    | { eventType: 'combat_started'; payload: CombatStartedEvent }
    | { eventType: 'combat_participants_joined'; payload: CombatParticipantsJoinedEvent }
    | { eventType: 'combat_turn_changed'; payload: CombatTurnChangedEvent }
    | { eventType: 'combat_ended'; payload: CombatEndedEvent }
    | { eventType: 'error'; payload: ErrorEvent }
    | { eventType: 'session_joined', payload: SessionJoinedEvent}
    | { eventType: 'classes_loaded'; payload: { templateId: string; name: string }[] }
    | { eventType: 'peace_proposal'; payload: PeaceProposalEvent }
    | { eventType: 'peace_result'; payload: PeaceResultEvent };

// Интерфейс для ошибок, приходящих в /user/queue/error
export interface ErrorEvent {
    message: string;
    errorCode?: string;
}

export interface Tile {
    coordinate: Hex;
    type: string; // "WALL", "FLOOR", "DOOR"
    occupiedBy?: string;
}

export interface GameMap {
    tiles: Tile[];
    // размеры, спавны и т.д.
}

// Добавим это в DTO сессии (нам понадобится позже)
export interface GameSessionState {
    sessionId: string;
    map: GameMap;
    entities: EntityStateDto[];
}

export interface TileDto {
    coordinate: Hex;
    type: string; // "WALL", "FLOOR", "DOOR"
    occupiedBy?: string;
}

export interface MapStateDto {
    tiles: TileDto[];
}

// Добавляем тип для полного состояния
export interface GameSessionStateDto {
    sessionId: string;
    mapState: MapStateDto;
    entities: EntityStateDto[];
    turnOrder?: string[]; 
    activeEntityId?: string;
}

export interface SessionJoinedEvent {
    sessionId: string;
    userId: string;
}

export interface PlayerClassDto {
    templateId: string;
    name: string;
}

export interface PeaceProposalEvent {
    sessionId: string;
    combatId: string;
    initiatorName: string;
    initiatorId: string;
}

export interface PeaceResultEvent {
    sessionId: string;
    combatId: string;
    success: boolean;
}