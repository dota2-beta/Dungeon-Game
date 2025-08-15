import React, { createContext, useReducer, type Dispatch, useContext, useState } from 'react';
import {
    type GameSessionStateDto,
    type EntityStatsUpdatedEvent,
    type EntityMovedEvent,
    type PlayerLeftEvent,
    type PlayerStateDto,
    type EntityAttackEvent,
    type CombatStartedEvent,
    type CombatNextTurnEvent,
    type EntityStateDto,
    type CombatEndedEvent,
    CombatOutcome,
    type MonsterStateDto,
    EntityStateType,
    type CasterStateUpdatedEvent,
    type AbilityCastedEvent,
    type AbilityStateDto,
    type PeaceProposalEvent,
    type CombatParticipantsJoinedEvent,
    type TeamInviteEvent,
    type TeamUpdatedEvent,
    type AbilityTemplateDto,
    type EntityTurnEndedEvent,
    type EntityDiedEvent,
} from '../types/dto';

export interface CombatState {
    combatId: string;
    turnOrder: string[];
    currentTurnEntityId: string;
}

export interface ExtendedGameSessionState extends GameSessionStateDto {
    lastAttack: { payload: EntityAttackEvent, timestamp: number } | null;
    lastDamage: { payload: EntityStatsUpdatedEvent, timestamp: number } | null;
    lastMove: { payload: EntityMovedEvent, timestamp: number } | null;
    lastAbilityCast: { payload: AbilityCastedEvent, timestamp: number } | null;
    activeCombat: CombatState | null;
    combatOutcomeInfo: { message: string; outcome: CombatOutcome } | null;
    selectedAbility: AbilityStateDto | null;
    activePeaceProposal: PeaceProposalState | null;
    notification: NotificationState | null
    activeTeamInvite: TeamInviteEvent | null
    contextMenu: ContextMenuState
    abilities: AbilityTemplateDto[];
    hoveredEntityId: string | null;
}

export type NotificationType = 'success' | 'error' | 'info';

export interface NotificationState {
    message: string;
    type: NotificationType;
    timestamp: number; // Для уникальности
}

const initialState: ExtendedGameSessionState = {
    sessionId: '',
    yourPlayerId: '',
    mapState: {
        tiles: [],
        spawnPoints: []
    },
    entities: [],
    lastAttack: null,
    lastDamage: null,
    lastMove: null,
    lastAbilityCast: null,
    activeCombat: null,
    combatOutcomeInfo: null,
    selectedAbility: null,
    activePeaceProposal: null,
    notification: null,
    activeTeamInvite: null,
    contextMenu: {
        isOpen: false,
        x: 0,
        y: 0,
        targetPlayer: null,
    },
    abilities: [],
    hoveredEntityId: null,
};

type GameAction =
    | { type: 'SET_INITIAL_STATE'; payload: GameSessionStateDto }
    | { type: 'ENTITY_MOVED'; payload: EntityMovedEvent }
    | { type: 'ENTITY_ATTACKED'; payload: EntityAttackEvent }
    | { type: 'ENTITY_TOOK_DAMAGE'; payload: EntityStatsUpdatedEvent } 
    | { type: 'CLEAR_COMBAT_ANIMATIONS' }
    | { type: 'CLEAR_MOVE_ANIMATION' }
    | { type: 'ADD_NEW_ENTITY'; payload: PlayerStateDto | MonsterStateDto } 
    | { type: 'REMOVE_ENTITY'; payload: PlayerLeftEvent }
    | { type: 'COMBAT_STARTED'; payload: CombatStartedEvent }
    | { type: 'COMBAT_PARTICIPANTS_JOINED'; payload: CombatParticipantsJoinedEvent }
    | { type: 'COMBAT_ENDED'; payload: CombatEndedEvent }
    | { type: 'CLEAR_COMBAT_OUTCOME' }
    | { type: 'NEXT_TURN'; payload: CombatNextTurnEvent }
    | { type: 'ABILITY_CASTED'; payload: AbilityCastedEvent }
    | { type: 'CASTER_STATE_UPDATED'; payload: CasterStateUpdatedEvent }
    | { type: 'CLEAR_ABILITY_ANIMATION' }
    | { type: 'SELECT_ABILITY'; payload: AbilityStateDto } 
    | { type: 'DESELECT_ABILITY' }
    | { type: 'PEACE_PROPOSAL_RECEIVED'; payload: PeaceProposalEvent }
    | { type: 'PEACE_PROPOSAL_CONCLUDED' }
    | { type: 'SHOW_NOTIFICATION'; payload: { message: string; type: NotificationType } }
    | { type: 'HIDE_NOTIFICATION' }
    | { type: 'TEAM_INVITE_RECEIVED'; payload: TeamInviteEvent }
    | { type: 'CLEAR_TEAM_INVITE' }
    | { type: 'TEAM_UPDATED'; payload: TeamUpdatedEvent }
    | { type: 'OPEN_CONTEXT_MENU'; payload: { x: number; y: number; targetPlayer: PlayerStateDto } }
    | { type: 'CLOSE_CONTEXT_MENU' }
    | { type: 'SET_ABILITY_TEMPLATES'; payload: AbilityTemplateDto[] }
    | { type: 'SET_HOVERED_ENTITY'; payload: string | null }
    | { type: 'ENTITY_TURN_ENDED'; payload: EntityTurnEndedEvent }
    | { type: 'ENTITY_DIED'; payload: EntityDiedEvent };

const updateEntityInState = <T extends EntityStateDto>(
    entities: T[], 
    entityId: string, 
    updates: Partial<T> 
): T[] => {
    return entities.map(entity => 
        entity.id === entityId ? { ...entity, ...updates } : entity
    );
};

export interface PeaceProposalState {
    initiatorId: string;
    initiatorName: string;
}

export interface ContextMenuState {
    isOpen: boolean;
    x: number;
    y: number;
    targetPlayer: PlayerStateDto | null;
}



const gameReducer = (state: ExtendedGameSessionState, action: GameAction): ExtendedGameSessionState => {
    if ('payload' in action) {
        console.log('GameContext Dispatch:', action.type, action.payload);
    } else {
        console.log('GameContext Dispatch:', action.type);
    }
    switch (action.type) {
        case 'SET_INITIAL_STATE':
            return { ...state, ...action.payload };
        
        case 'ENTITY_MOVED':
            return {
                ...state,
                entities: updateEntityInState(
                    state.entities, 
                    action.payload.entityId, 
                    { currentAP: action.payload.currentAP } 
                ),
                lastMove: { payload: action.payload, timestamp: Date.now() }
            };
        
        case 'ENTITY_ATTACKED':
            return {
                ...state,
                entities: updateEntityInState(
                    state.entities, 
                    action.payload.attackerEntityId, 
                    { currentAP: action.payload.attackerCurrentAP }
                ),
                lastAttack: { payload: action.payload, timestamp: Date.now() }
            };
        
            case 'ENTITY_TOOK_DAMAGE': { 
                const { targetEntityId, currentHp, currentDefense, dead, healToHp } = action.payload;
    
                const updatedEntities = state.entities.map(entity => {
                    if (entity.id !== targetEntityId) {
                        return entity;
                    }
    
                    if (healToHp && healToHp > 0) {
                        return { ...entity, currentHp: currentHp };
                    }
    
                    // КЛЮЧЕВОЕ ИСПРАВЛЕНИЕ:
                    // Мы берем `currentDefense` из payload'а и записываем его в поле `defense` нашего объекта.
                    return {
                        ...entity,
                        currentHp: currentHp,
                        defense: currentDefense, // <-- ИСПРАВЛЕНО ЗДЕСЬ
                        dead: dead,
                    };
                });
    
                return {
                    ...state,
                    entities: updatedEntities,
                    lastDamage: { payload: action.payload, timestamp: Date.now() }
                };
            }

        case 'CLEAR_COMBAT_ANIMATIONS':
            return { ...state, lastAttack: null, lastDamage: null };
        
        case 'CLEAR_MOVE_ANIMATION':
            return { ...state, lastMove: null };
            
        case 'ADD_NEW_ENTITY':
            if (state.entities.some(e => e.id === action.payload.id)) return state;
            return { ...state, entities: [...state.entities, action.payload] };
        
        case 'REMOVE_ENTITY':
            return { ...state, entities: state.entities.filter(e => e.id !== action.payload.entityId) };
        
        case 'COMBAT_STARTED': {
            const { combatants, initialTurnOrder, combatId } = action.payload;
            const combatantsMap = new Map(combatants.map(c => [c.id, c]));

            const updatedEntities = state.entities.map(entity => {
                const freshData = combatantsMap.get(entity.id);
                if (freshData) {
                    const mergedEntity = { ...entity, ...freshData };
                    if (entity.type === 'PLAYER') {
                        return mergedEntity as PlayerStateDto;
                    } else {
                        return mergedEntity as MonsterStateDto;
                    }
                }
                return entity;
            });

            const isPlayerInCombat = combatants.some(
                combatant => combatant.id === state.yourPlayerId
            );

            return {
                ...state,
                entities: updatedEntities,
                activeCombat: isPlayerInCombat
                    ? {
                        combatId: combatId,
                        turnOrder: initialTurnOrder,
                        currentTurnEntityId: initialTurnOrder[0],
                      }
                    : state.activeCombat,
            };
        }

        case 'COMBAT_PARTICIPANTS_JOINED': {
            const { combatId, participantIds, turnOrder } = action.payload;

            const isPlayerJoining = participantIds.includes(state.yourPlayerId);
            
            if (!isPlayerJoining && state.activeCombat?.combatId !== combatId) {
                return state;
            }

            const updatedEntities = state.entities.map(entity => {
                if (participantIds.includes(entity.id)) {
                    return {
                        ...entity,
                        state: EntityStateType.COMBAT,
                        currentAP: entity.maxAP,
                    };
                }
                return entity;
            });

            return {
                ...state,
                entities: updatedEntities,
                activeCombat: {
                    combatId: combatId,
                    turnOrder: turnOrder,
                    currentTurnEntityId: state.activeCombat?.currentTurnEntityId ?? turnOrder[0],
                },
            };
        }

        case 'NEXT_TURN':
            if (!state.activeCombat || state.activeCombat.combatId !== action.payload.combatId) {
                return state;
            }
            const { currentTurnEntityId, currentAP, abilityCooldowns } = action.payload;

            return {
                ...state,
                entities: updateEntityInState(
                    state.entities,
                    currentTurnEntityId,
                    {
                        currentAP: currentAP,        
                        abilities: abilityCooldowns,  
                    }
                ),
                activeCombat: {
                    ...state.activeCombat,
                    currentTurnEntityId: currentTurnEntityId,
                },
                selectedAbility: null,
            };

        case 'COMBAT_ENDED': {
                const player = state.entities.find(e => e.id === state.yourPlayerId);
                if (!player) return state;
                const amIVictorious = action.payload.winningTeamId != null &&
                                      (player.teamId === action.payload.winningTeamId || player.id === action.payload.winningTeamId);
            
                const message = amIVictorious ? 'VICTORY!' : 'DEFEAT';
                const outcome = amIVictorious ? CombatOutcome.VICTORY : CombatOutcome.DEFEAT;
            
                const entitiesAfterCombat = state.entities.map(e => e.dead ? e : { ...e, state: EntityStateType.EXPLORING });

                return {
                    ...state,
                    entities: entitiesAfterCombat,
                    activeCombat: null,
                    combatOutcomeInfo: { message, outcome },
                    activePeaceProposal: null,
                };
            }
        
        case 'CLEAR_COMBAT_OUTCOME':
            return {
                ...state,
                combatOutcomeInfo: null
            };

        case 'CASTER_STATE_UPDATED': {
            const { casterId, newCurrentAP, abilityCooldowns } = action.payload;
            
            const updatedEntities = updateEntityInState(
                state.entities,
                casterId,
                {
                    currentAP: newCurrentAP,
                    abilities: abilityCooldowns,
                }
            );

            return {
                ...state,
                entities: updatedEntities,
            };
        }

        case 'CLEAR_ABILITY_ANIMATION': {
            return {
                ...state,
                lastAbilityCast: null,
            };
        }
        case 'SELECT_ABILITY': {
            if (state.selectedAbility?.abilityTemplateId === action.payload.abilityTemplateId) {
                return { ...state, selectedAbility: null };
            }
            return { ...state, selectedAbility: action.payload };
        }

        case 'DESELECT_ABILITY':
            return { ...state, selectedAbility: null };
    
        case 'ABILITY_CASTED': {
            return {
                ...state,
                lastAbilityCast: { payload: action.payload, timestamp: Date.now() },
                selectedAbility: null, 
            };
        }
        case 'PEACE_PROPOSAL_RECEIVED':
            return {
                ...state,
                activePeaceProposal: {
                    initiatorId: action.payload.initiatorId,
                    initiatorName: action.payload.initiatorName,
                },
            };
        
        case 'PEACE_PROPOSAL_CONCLUDED':
            return {
                ...state,
                activePeaceProposal: null,
            };
        case 'SHOW_NOTIFICATION':
            return {
                ...state,
                notification: {
                    message: action.payload.message,
                    type: action.payload.type,
                    timestamp: Date.now(),
                },
            };

        case 'HIDE_NOTIFICATION':
            return {
                ...state,
                notification: null,
            };
            
        case 'TEAM_INVITE_RECEIVED':
            return {
                ...state,
                    activeTeamInvite: action.payload,
                };
        
        case 'CLEAR_TEAM_INVITE':
            return {
                ...state,
                activeTeamInvite: null,
            };
    
        case 'TEAM_UPDATED': {
            const { teamId, memberIds } = action.payload;
            const memberIdSet = new Set(memberIds);

            // --- DEBUG LOGGING ---
            console.group(`[DEBUG] Reducer: TEAM_UPDATED for teamId: ${teamId}`);
            const playerBefore = state.entities.find(e => e.id === state.yourPlayerId);
            console.log(`Payload memberIds:`, memberIds);
            console.log(`Your player's teamId BEFORE update:`, playerBefore?.teamId);
            // --- END DEBUG ---

            const updatedEntities = state.entities.map(entity => {
                if (memberIdSet.has(entity.id)) {
                    return { ...entity, teamId: teamId };
                }
                if (entity.teamId === teamId && !memberIdSet.has(entity.id)) {
                    return { ...entity, teamId: entity.id };
                }
                return entity;
            });

            // --- DEBUG LOGGING ---
            const playerAfter = updatedEntities.find(e => e.id === state.yourPlayerId);
            console.log(`Your player's teamId AFTER update:`, playerAfter?.teamId);
            console.groupEnd();
            // --- END DEBUG ---

            return {
                ...state,
                entities: updatedEntities,
            };
        }
        case 'OPEN_CONTEXT_MENU':
            return {
                ...state,
                contextMenu: {
                    isOpen: true,
                    x: action.payload.x,
                    y: action.payload.y,
                    targetPlayer: action.payload.targetPlayer,
                },
            };

        case 'CLOSE_CONTEXT_MENU':
            return {
                ...state,
                contextMenu: { ...state.contextMenu, isOpen: false },
            };
        
        case 'SET_ABILITY_TEMPLATES':
            console.log('[DEBUG] Reducer: Saving ability templates into state.');
            return {
                ...state,
                abilities: action.payload,
            };
        case 'SET_HOVERED_ENTITY':
            return { ...state, hoveredEntityId: action.payload };
        case 'REMOVE_ENTITY':
            return { ...state, entities: state.entities.filter(e => e.id !== action.payload.entityId) };

        case 'ENTITY_TURN_ENDED':
            console.log(`Turn ended for entity: ${action.payload.currentTurnEntityId}`);
            return state;
    
        case 'ENTITY_DIED':
            return {
                ...state,
                entities: state.entities.map(entity => 
                    entity.id === action.payload.entityId 
                        ? { ...entity, dead: true, currentHp: 0 } 
                        : entity
                ),
            };

        default:
            return state;
    }
};

interface GameContextProps {
    gameState: ExtendedGameSessionState;
    dispatch: Dispatch<GameAction>;
    errorMessage: string;
    setErrorMessage: (message: string) => void;
}

const GameContext = createContext<GameContextProps>({
    gameState: initialState,
    dispatch: () => null,
    errorMessage: '',
    setErrorMessage: () => {},
});

export const GameProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [gameState, dispatch] = useReducer(gameReducer, initialState);
    const [errorMessage, setErrorMessage] = useState<string>('');
    const contextValue = { gameState, dispatch, errorMessage, setErrorMessage };
    return (
        <GameContext.Provider value={contextValue}>
            {children}
        </GameContext.Provider>
    );
};

export const useGame = () => useContext(GameContext);

export type { PlayerStateDto };
