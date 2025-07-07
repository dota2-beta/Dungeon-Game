import React, { createContext, useReducer, type Dispatch, useContext, type SetStateAction, useState } from 'react';
import type { GameSessionState, EntityClientState, Point, EntityMovedPayload, DamageTakenPayload, PlayerLeftPayload } from '../types/dto';
import type { PlayerClientState, MonsterClientState } from '../types/dto';

const initialState: GameSessionState = {
    sessionId: '',
    yourPlayerId: '',
    mapState: { 
        width: 0,
        height: 0,
        tiles: [],
    },
    entities: [],
    // gameObjects: [],
    // activeCombats: [], 
};

type GameAction =
    | { type: 'SET_INITIAL_STATE'; payload: GameSessionState }
    | { type: 'UPDATE_ENTITY_POSITION'; payload: EntityMovedPayload }
    | { type: 'UPDATE_ENTITY_HP'; payload: DamageTakenPayload }
    | { type: 'ADD_NEW_ENTITY'; payload: PlayerClientState } 
    | { type: 'REMOVE_ENTITY'; payload: PlayerLeftPayload };

const gameReducer = (state: GameSessionState, action: GameAction): GameSessionState => {
    switch (action.type) {
        case 'SET_INITIAL_STATE':
            return {
                ...state,
                ...action.payload,
            };
        
        case 'UPDATE_ENTITY_POSITION':
            return {
                ...state,
                entities: state.entities.map(entity =>
                    entity.id === action.payload.entityId
                        ? { ...entity, position: action.payload.newPosition }
                        : entity
                ),
            };
        
        case 'UPDATE_ENTITY_HP':
            return {
                ...state,
                entities: state.entities.map(entity =>
                    entity.id === action.payload.targetId
                        ? { ...entity, currentHp: action.payload.newHp }
                        : entity
                ),
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
            return state;
    }
};

interface GameContextProps {
    gameState: GameSessionState;
    dispatch: Dispatch<GameAction>;
    errorMessage: string;
    setErrorMessage: Dispatch<SetStateAction<string>>;
}

const GameContext = createContext<GameContextProps>({
    gameState: initialState,
    dispatch: () => null,
    errorMessage: '',
    setErrorMessage: () => null,
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
