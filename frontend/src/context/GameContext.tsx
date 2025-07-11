import React, { createContext, useReducer, type Dispatch, useContext, useState } from 'react';
import type { 
    GameSessionState, 
    EntityStatsUpdatedEvent, 
    EntityMovedEvent, 
    PlayerLeftPayload, 
    PlayerState,
    EntityAttackEvent 
} from '../types/dto';


interface ExtendedGameSessionState extends GameSessionState {
    lastAttack: {
        payload: EntityAttackEvent,
        timestamp: number,
    } | null;
    lastDamage: {
        payload: EntityStatsUpdatedEvent,
        timestamp: number,
    } | null;
}

const initialState: ExtendedGameSessionState = {
    sessionId: '',
    yourPlayerId: '',
    mapState: { 
        width: 0,
        height: 0,
        tiles: [],
        spawnPoints: []
    },
    entities: [],
    lastAttack: null,
    lastDamage: null,
};

type GameAction =
    | { type: 'SET_INITIAL_STATE'; payload: GameSessionState }
    | { type: 'UPDATE_ENTITY_POSITION'; payload: EntityMovedEvent }
    | { type: 'ENTITY_ATTACKED'; payload: EntityAttackEvent }
    | { type: 'ENTITY_TOOK_DAMAGE'; payload: EntityStatsUpdatedEvent } 
    | { type: 'CLEAR_COMBAT_ANIMATIONS' }
    | { type: 'ADD_NEW_ENTITY'; payload: PlayerState }
    | { type: 'REMOVE_ENTITY'; payload: PlayerLeftPayload };


const gameReducer = (state: ExtendedGameSessionState, action: GameAction): ExtendedGameSessionState => {
    switch (action.type) {
        case 'SET_INITIAL_STATE':
            return { ...initialState, ...action.payload };
        
        case 'UPDATE_ENTITY_POSITION':
            return {
                ...state,
                entities: state.entities.map(entity =>
                    entity.id === action.payload.entityId
                        ? { ...entity, position: action.payload.newPosition }
                        : entity
                ),
            };
        
        case 'ENTITY_ATTACKED':
            return {
                ...state,
                lastAttack: { payload: action.payload, timestamp: Date.now() }
            };

        case 'ENTITY_TOOK_DAMAGE':
            const updatedEntities = state.entities.map(entity => {
                if (entity.id !== action.payload.targetEntityId) {
                    return entity;
                }
                return {
                    ...entity,
                    currentHp: action.payload.currentHp,
                    isDead: action.payload.dead ?? entity.isDead 
                };
            });
            return {
                ...state,
                entities: updatedEntities,
                lastDamage: { payload: action.payload, timestamp: Date.now() }
            };

        case 'CLEAR_COMBAT_ANIMATIONS':
            return {
                ...state,
                lastAttack: null,
                lastDamage: null,
            };
            
        case 'ADD_NEW_ENTITY':
            if (state.entities.some(e => e.id === action.payload.id)) {
                return state;
            }
            return {
                ...state,
                entities: [...state.entities, action.payload]
            };
        
        case 'REMOVE_ENTITY':
            return {
                ...state,
                entities: state.entities.filter(e => e.id !== action.payload.entityId)
            };
        
        default:
            const exhaustiveCheck: never = action;
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

    const contextValue = {
        gameState,
        dispatch,
        errorMessage,
        setErrorMessage,
    };

    return (
        <GameContext.Provider value={contextValue}>
            {children}
        </GameContext.Provider>
    );
};

export const useGame = () => useContext(GameContext);