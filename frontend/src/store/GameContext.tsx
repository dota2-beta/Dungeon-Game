import React, { createContext, useReducer, useContext, type ReactNode } from 'react';
import { type EntityStateDto, type GameEvent, type MapStateDto, EntityState } from '../types/game';

interface GameState {
    sessionId: string | null;
    userId: string | null;
    entities: Record<string, EntityStateDto>;
    map: MapStateDto | null; // <--- ВОТ ЭТО ПОЛЕ, которого не хватало
    logs: string[];
    isConnected: boolean;
    turnOrder: string[];                // Список ID участников боя
    activeEntityId: string | null;      // ID того, чей сейчас ход
    hoveredEntityId: string | null;     // ID сущности под курсором мыши
    playerClasses: Array<{ templateId: string; name: string }>;
    peaceProposal: { initiatorName: string; initiatorId: string } | null;
}

const initialState: GameState = {
    sessionId: null,
    userId: null,
    entities: {},
    map: null, // <--- Инициализация
    logs: [],
    isConnected: false,
    turnOrder: [],
    activeEntityId: null,
    hoveredEntityId: null,
    playerClasses: [],
    peaceProposal: null
};

type Action = 
    | { type: 'SOCKET_CONNECTED' }
    | { type: 'SOCKET_DISCONNECTED'}
    | { type: 'GAME_EVENT'; payload: GameEvent }
    | { type: 'STATE_SNAPSHOT'; payload: any }
    | { type: 'SET_HOVER'; payload: string | null }; ;

function gameReducer(state: GameState, action: Action): GameState {
    switch (action.type) {
        case 'SOCKET_CONNECTED':
            return { ...state, isConnected: true };

        case 'SOCKET_DISCONNECTED': 
             return { ...state, isConnected: false };

        case 'STATE_SNAPSHOT': {
            const { sessionId, mapState, entities } = action.payload; // payload приходит из GameSocket
            
            // Превращаем массив в Map для удобства
            const entitiesMap = (entities || []).reduce((acc: any, entity: EntityStateDto) => {
                acc[entity.id] = entity;
                return acc;
            }, {});

            return {
                ...state,
                sessionId: sessionId,
                map: mapState, // Сохраняем карту
                entities: entitiesMap,
                logs: [...state.logs, "✅ Map and Entities loaded."]
            };
        }

        case 'SET_HOVER':
            return {
                ...state,
                hoveredEntityId: action.payload
            };

        case 'GAME_EVENT':
            const event = action.payload;
            
            // Логируем все события для отладки
            const newLogs = [...state.logs, `Event: ${event.eventType}`];

            switch (event.eventType) {
                case 'session_created':
                    return {
                        ...state,
                        sessionId: event.payload.sessionId,
                        userId: event.payload.userId,
                        logs: [...newLogs, `Session created! ID: ${event.payload.sessionId}`]
                    };

                case 'entity_joined': {
                    const entity = event.payload.entityStateDto;
                    return {
                        ...state,
                        entities: {
                            ...state.entities,
                            [entity.id]: entity // Добавляем или обновляем сущность
                        },
                        logs: [...newLogs, `${entity.name} joined the game`]
                    };
                }

                case 'entity_moved': {
                    const { entityId, newPosition, currentAP } = event.payload;
                    
                    const oldEntity = state.entities[entityId];
                    if (!oldEntity) return state;

                    const updatedEntity = { 
                        ...oldEntity, 
                        position: newPosition,
                        currentAP: currentAP !== undefined ? currentAP : oldEntity.currentAP
                    };

                    return {
                        ...state,
                        entities: {
                            ...state.entities,
                            [entityId]: updatedEntity
                        },
                        logs: [...newLogs, `${oldEntity.name} moved`]
                    };
                }

                case 'entity_attack': {
                    const { attackerEntityId, targetEntityId, attackerCurrentAP } = event.payload;
                    
                    const attacker = state.entities[attackerEntityId];
                    const target = state.entities[targetEntityId];

                    let newEntities = state.entities;

                    // Если атакующий существует (это не ловушка), обновляем ему AP
                    if (attacker) {
                        newEntities = {
                            ...newEntities,
                            [attackerEntityId]: {
                                ...attacker,
                                currentAP: attackerCurrentAP // <--- Вот здесь списываются AP на фронте
                            }
                        };
                    }

                    return {
                        ...state,
                        entities: newEntities,
                        logs: [...newLogs, `${attacker?.name || 'Unknown'} attacked ${target?.name || 'Unknown'}`]
                    };
                }

                case 'error': {
                    return {
                        ...state,
                        logs: [...newLogs, `❌ Error: ${event.payload.message}`]
                    };
                }

                case 'session_joined':
                    return {
                        ...state,
                        sessionId: event.payload.sessionId,
                        userId: event.payload.userId,
                        logs: [...newLogs, `Joined session: ${event.payload.sessionId}`]
                    };
                
                case 'entity_stats_updated': {
                    const { entityId, currentHp, currentDefense, damageToHp, absorbedByArmor, isDead } = event.payload;
                    
                    const targetEntity = state.entities[entityId];
                    if (!targetEntity) return state;

                    // Лог для чата
                    const dmgLog = damageToHp > 0 ? ` took ${damageToHp} dmg` : '';
                    const armorLog = absorbedByArmor > 0 ? ` (${absorbedByArmor} blocked)` : '';
                    const deathLog = isDead ? ' and DIED ☠️' : '';

                    return {
                        ...state,
                        entities: {
                            ...state.entities,
                            [entityId]: {
                                ...targetEntity,
                                currentHp: currentHp,       // Обновляем HP
                                defense: currentDefense,    // Обновляем Броню
                                // Если с бэка приходит isDead, можно менять иконку или удалять,
                                // но пока просто обновим цифры.
                            }
                        },
                        logs: [...newLogs, `${targetEntity.name}${dmgLog}${armorLog}${deathLog}`]
                    };
                }
                
                case 'combat_started': {
                    const { initialTurnOrder, combatants } = event.payload;
                    
                    const updatedEntities = { ...state.entities };
                    
                    combatants.forEach((c: EntityStateDto) => {
                        // 🔥 ВАЖНО: Мы мержим старый объект с новым.
                        // Это гарантирует, что если у нас уже был userId в старом объекте, 
                        // а в новом пришел null (например), мы сохраним старый userId.
                        const existing = updatedEntities[c.id];
                        updatedEntities[c.id] = { 
                            ...existing, 
                            ...c,
                            // Если вдруг с бэка пришел пустой userId, а у нас он был - оставляем наш
                            userId: c.userId || existing?.userId 
                        };
                    });

                    return {
                        ...state,
                        turnOrder: initialTurnOrder,
                        entities: updatedEntities,
                        // 🔥 ВАЖНО: При старте боя активным становится ПЕРВЫЙ в очереди
                        activeEntityId: initialTurnOrder.length > 0 ? initialTurnOrder[0] : null,
                        logs: [...newLogs, "⚔️ Combat Started!"]
                    };
                }

                case 'combat_turn_changed': {
                    const { activeEntityId, currentAP } = event.payload;
                    
                    // 1. Находим того, чей сейчас ход
                    const activeEnt = state.entities[activeEntityId];
                    let newEntities = state.entities;

                    // 2. Обновляем ему AP (сервер присылает восстановленное значение)
                    if (activeEnt) {
                        newEntities = {
                            ...state.entities,
                            [activeEntityId]: { 
                                ...activeEnt, 
                                currentAP: currentAP 
                            }
                        };
                    }

                    return {
                        ...state,
                        activeEntityId: activeEntityId, // <-- Переключаем "золотую рамку"
                        entities: newEntities,
                        logs: [...newLogs, `Turn passed to ${activeEnt ? activeEnt.name : 'Unknown'}`]
                    };
                }

                case 'combat_participants_joined': {
                    const { turnOrder, participants, activeEntityId } = event.payload;
                    
                    console.log("New participants joined:", participants);

                    // 1. Обновляем (мержим) сущности
                    // Нам нужно обновить их state на COMBAT и, возможно, другие поля
                    const updatedEntities = { ...state.entities };
                    
                    participants.forEach((p: EntityStateDto) => {
                        // Если сущность уже была (мы её видели), обновляем ей статус
                        // Если не было (прибежала из тумана войны) — добавляем
                        updatedEntities[p.id] = { 
                            ...(updatedEntities[p.id] || {}), 
                            ...p 
                        };
                    });

                    return {
                        ...state,
                        entities: updatedEntities,
                        turnOrder: turnOrder,
                        activeEntityId: activeEntityId,
                        logs: [...newLogs, `⚠️ Battle expanded! Active: ${state.entities[activeEntityId]?.name}`]
                    };
                }

                case 'combat_ended': {
                    const { outcome } = event.payload;

                    if (outcome === 'MERGED') {
                        return {
                            ...state,
                            logs: [...newLogs, "⚠️ Combat merged! Switching to new battle..."]
                        };
                    }

                    const updatedEntities = { ...state.entities };
                    
                    Object.keys(updatedEntities).forEach(key => {
                        updatedEntities[key] = { 
                            ...updatedEntities[key], 
                            state: EntityState.EXPLORING 
                        };
                    });

                    return {
                        ...state,
                        entities: updatedEntities,
                        turnOrder: [],
                        activeEntityId: null,
                        logs: [...newLogs, `🕊️ Combat Ended. Result: ${outcome}`]
                    };
                }

                case 'peace_proposal':
                    return {
                        ...state,
                        peaceProposal: { 
                            initiatorName: event.payload.initiatorName,
                            initiatorId: event.payload.initiatorId // <--- Сохраняем ID
                        },
                        logs: [...newLogs, `🏳️ ${event.payload.initiatorName} proposes peace`]
                    };

                case 'peace_result':
                    return {
                        ...state,
                        peaceProposal: null,
                        logs: [...newLogs, event.payload.success 
                            ? "🕊️ Peace agreed! Combat ending..." 
                            : "⚔️ Peace rejected! Fight on!"]
                    };
                
                case 'classes_loaded':
                    return { 
                        ...state, 
                        playerClasses: event.payload
                    };

                default:
                    return { ...state, logs: newLogs };
            }
        
        default:
            return state;
    }
}

const GameContext = createContext<{
    state: GameState;
    dispatch: React.Dispatch<Action>;
} | undefined>(undefined);

export const GameProvider = ({ children }: { children: ReactNode }) => {
    const [state, dispatch] = useReducer(gameReducer, initialState);
    return (
        <GameContext.Provider value={{ state, dispatch }}>
            {children}
        </GameContext.Provider>
    );
};

export const useGameState = () => {
    const context = useContext(GameContext);
    if (!context) throw new Error("useGameState must be used within a GameProvider");
    return context;
};