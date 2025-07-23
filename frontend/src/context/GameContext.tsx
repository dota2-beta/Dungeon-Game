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
    type MonsterStateDto
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
    activeCombat: CombatState | null;
    combatOutcomeInfo: { message: string; outcome: CombatOutcome } | null;
}

const initialState: ExtendedGameSessionState = {
    sessionId: '',
    yourPlayerId: '',
    mapState: { 
        radius: 0,
        tiles: [],
        spawnPoints: []
    },
    entities: [],
    lastAttack: null,
    lastDamage: null,
    lastMove: null,
    activeCombat: null,
    combatOutcomeInfo: null,
};

type GameAction =
    | { type: 'SET_INITIAL_STATE'; payload: GameSessionStateDto }
    | { type: 'ENTITY_MOVED'; payload: EntityMovedEvent }
    | { type: 'ENTITY_ATTACKED'; payload: EntityAttackEvent }
    | { type: 'ENTITY_TOOK_DAMAGE'; payload: EntityStatsUpdatedEvent } 
    | { type: 'CLEAR_COMBAT_ANIMATIONS' }
    | { type: 'CLEAR_MOVE_ANIMATION' }
    | { type: 'ADD_NEW_ENTITY'; payload: PlayerStateDto }
    | { type: 'REMOVE_ENTITY'; payload: PlayerLeftEvent }
    | { type: 'COMBAT_STARTED'; payload: CombatStartedEvent }
    | { type: 'COMBAT_ENDED'; payload: CombatEndedEvent }
    | { type: 'CLEAR_COMBAT_OUTCOME' }
    | { type: 'NEXT_TURN'; payload: CombatNextTurnEvent };

const updateEntityInState = <T extends EntityStateDto>(
    entities: T[], 
    entityId: string, 
    updates: Partial<T> 
): T[] => {
    return entities.map(entity => 
        entity.id === entityId ? { ...entity, ...updates } : entity
    );
};


const gameReducer = (state: ExtendedGameSessionState, action: GameAction): ExtendedGameSessionState => {
    switch (action.type) {
        case 'SET_INITIAL_STATE':
            return { ...initialState, ...action.payload };
        
        case 'ENTITY_MOVED':
            return {
                ...state,
                entities: updateEntityInState(
                    state.entities, 
                    action.payload.entityId, 
                    { currentAP: action.payload.currentAp } 
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
                const updatedEntities = state.entities.map(entity => {
                    if (entity.id !== action.payload.targetEntityId) {
                        return entity;
                    }
                    return {
                        ...entity,
                        currentHp: action.payload.currentHp,
                        dead: action.payload.dead
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

            case 'COMBAT_STARTED':
            return {
                ...state,
                activeCombat: {
                    combatId: action.payload.combatId,
                    turnOrder: action.payload.initialTurnOrder, 
                    currentTurnEntityId: action.payload.initialTurnOrder[0],
                }
            };

        case 'NEXT_TURN':
            if (!state.activeCombat || state.activeCombat.combatId !== action.payload.combatId) {
                return state;
            }
            return {
                ...state,
                entities: updateEntityInState(
                    state.entities, 
                    action.payload.currentTurnEntityId, 
                    { currentAP: action.payload.currentAp }
                ),
                activeCombat: {
                    ...state.activeCombat,
                    currentTurnEntityId: action.payload.currentTurnEntityId,
                }
            };

        case 'COMBAT_ENDED': {
                const player = state.entities.find(e => e.id === state.yourPlayerId);
                if (!player) return state;
                const amIVictorious = action.payload.winningTeamId != null &&
                                      (player.teamId === action.payload.winningTeamId || player.id === action.payload.winningTeamId);
            
                const message = amIVictorious ? 'VICTORY!' : 'DEFEAT';
                const outcome = amIVictorious ? CombatOutcome.VICTORY : CombatOutcome.DEFEAT;
            
                return {
                    ...state,
                    activeCombat: null,
                    combatOutcomeInfo: { message, outcome }
                };
            }
        
        case 'CLEAR_COMBAT_OUTCOME':
            return {
                ...state,
                combatOutcomeInfo: null
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