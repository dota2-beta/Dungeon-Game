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
    END_BY_AGREEMENT = 'END_BY_AGREEMENT', 
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

/**
 * Описывает состояние одной способности у конкретной сущности.
 * Это то, что будет лежать в "ячейке" заклинаний.
 */
export interface AbilityStateDto {
    abilityTemplateId: string;      // ID шаблона
    turnCooldown: number;      // Текущий кулдаун в ходах
    cooldownEndTime: number;   // Временная метка в мс, когда КД закончится
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
    abilities: AbilityStateDto[];
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
    currentAP?: number;
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
    damageToHp?: number; 
    healToHp?: number;  
    currentHp: number;
    absorbedByArmor?: number;
    currentDefense?: number;
    dead: boolean;
}

/**
 * Событие: Способность была использована.
 * Сигнал для рендерера, чтобы проиграть анимацию.
 */
export interface AbilityCastedEvent {
    casterId: string;
    abilityTemplateId: string;
    targetHex: Hex;
}

/**
 * Событие: Состояние кастера обновилось после использования способности.
 * Сигнал для UI, чтобы обновить AP и кулдауны на панели действий.
 */
export interface CasterStateUpdatedEvent {
    casterId: string;
    newCurrentAP: number;
    abilityCooldowns: AbilityStateDto[];
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
    currentAP: number;
    abilityCooldowns: AbilityStateDto[];
}

export interface CombatParticipantsJoinedEvent {
    combatId: string;
    participantIds: string[]; // ID новых участников
    turnOrder: string[];      // Новый, полный порядок ходов
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
    actionType: 'MOVE' | 'ATTACK' | 'END_TURN' | 'CAST_SPELL';
    targetId?: string;
    targetHex?: Hex;
    abilityId?: string;
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

/**
 * Событие от сервера: Игрок инициировал голосование о мире.
 * Соответствует вашему PeaceProposalEvent.java.
 */
export interface PeaceProposalEvent {
    initiatorId: string;
    initiatorName: string;
}

/**
 * Событие от сервера: Голосование о мире завершено.
 * Соответствует вашему PeaceProposalResultEvent.java.
 */
export interface PeaceProposalResultEvent {
    wasAccepted: boolean;
    rejectorName: string | null;
}


// =================================================================
// Client Actions (Действия, отправляемые клиентом на сервер)
// =================================================================

/**
 * Запрос от клиента: Игрок предлагает мир.
 * Тело запроса пустое.
 */
export interface ProposePeaceRequest {}

/**
 * Запрос от клиента: Игрок отвечает на предложение о мире.
 * Соответствует вашему RespondToPeaceRequest.java.
 */
export interface RespondToPeaceRequest {
    accepted: boolean;
}