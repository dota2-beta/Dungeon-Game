import React, { useEffect, useState, type FC } from 'react';
import { connect, subscribe, publish, disconnect } from './api/websocketService';
import { GameProvider, useGame } from './context/GameContext';
import type {
    GameSessionStateDto,
    GameUpdatePayload,
    EntityMovedEvent,
    EntityStatsUpdatedEvent,
    PlayerJoinedEvent,
    PlayerLeftEvent,
    EntityAttackEvent,
    ErrorEvent,
    PlayerStateDto,
    CombatStartedEvent,
    CombatNextTurnEvent,
    CombatEndedEvent,
    CasterStateUpdatedEvent,
    AbilityCastedEvent,
    PeaceProposalEvent,
    PeaceProposalResultEvent,
    CombatParticipantsJoinedEvent
} from './types/dto';
import GameCanvas from './components/GameCanvas';
import PlayerHUD from './components/PlayerHUD';
import type { IFrame } from '@stomp/stompjs';
import TurnOrder from './components/TurnOrder';
import ActionBar from './components/ActionBar';
import CombatOutcomeNotification from './components/CombatOutcomeNotification';
import AbilityBar from './components/AbilityBar';
import MainHUD from './components/MainHUD';
import PeaceProposalUI from './components/PeaceProposalUI';
import NotificationUI from './components/NotificationUI';

const Game: FC = () => {
    const { gameState, dispatch, setErrorMessage } = useGame();
    const { errorMessage } = useGame();

    const [sessionId, setSessionId] = useState<string | null>(null);
    const [isConnectedToServer, setIsConnectedToServer] = useState<boolean>(false);
    const [joinSessionId, setJoinSessionId] = useState<string>('');

    useEffect(() => {
        const onStompConnect = (frame: IFrame) => {
            setIsConnectedToServer(true);
            setErrorMessage('');
            console.log('STOMP: Connected. Setting up general subscriptions...');

            subscribe<{ status: string; sessionId?: string; message?: string }>(
                '/user/queue/create-session-response',
                (response) => {
                    console.log('Received create-session response:', response);
                    if (response.status === 'success' && response.sessionId) {
                        setSessionId(response.sessionId);
                    } else {
                        setErrorMessage(response.message || 'Failed to create session.');
                    }
                }
            );

            subscribe<{ status: string; message?: string }>(
                '/user/queue/join-session-response',
                (response) => {
                    if (response.status !== 'success') {
                        setErrorMessage(response.message || 'Failed to join session.');
                    }
                }
            );

            subscribe<ErrorEvent>(
                '/user/queue/errors',
                (errorPayload) => {
                    console.error(
                        `Received error from server. Code: [${errorPayload.errorCode || 'NONE'}], Message: "${errorPayload.message}"`
                    );
                    setErrorMessage(errorPayload.message || "An unknown error occurred.");
                }
            );

            subscribe<GameUpdatePayload<any>>(
                '/user/queue/events', // Адрес из WebSocketDestinations.PRIVATE_EVENTS_QUEUE
                (update) => {
                    console.log(`%c[PRIVATE EVENT] Received: ${update.actionType}`, 'color: #9b59b6; font-weight: bold;', update.payload);
                    
                    // Обрабатываем события так же, как и публичные
                    switch (update.actionType) {
                        case 'peace_proposal':
                            dispatch({ type: 'PEACE_PROPOSAL_RECEIVED', payload: update.payload as PeaceProposalEvent });
                            break;
                        case 'peace_proposal_result': {
                            const result = update.payload as PeaceProposalResultEvent;
                            if (result.wasAccepted) {
                                dispatch({ 
                                    type: 'SHOW_NOTIFICATION', 
                                    payload: { message: 'Peace proposal was accepted!', type: 'success' }
                                });
                            } else {
                                dispatch({
                                    type: 'SHOW_NOTIFICATION',
                                    payload: { 
                                        message: `Peace proposal was rejected by ${result.rejectorName ?? 'Someone'}!`, 
                                        type: 'error' 
                                    }
                                });
                            }
                            dispatch({ type: 'PEACE_PROPOSAL_CONCLUDED' });
                            break;
                        }
                        // Здесь можно будет обрабатывать и другие личные события в будущем
                    }
                }
            );
            
        };

        const onError = (error: any) => {
            console.error("Connection error:", error);
            setIsConnectedToServer(false);
            setErrorMessage('Failed to connect to the server. Please refresh.');
        };

        connect(onStompConnect, onError);

        return () => {
            disconnect();
        };
    }, [setErrorMessage, dispatch]);

    useEffect(() => {
        if (!isConnectedToServer || !sessionId) return;

        console.log(`Subscribing to session-specific topics for session: ${sessionId}`);

        const initialStateSubscription = subscribe<GameSessionStateDto>(
            `/user/queue/session/${sessionId}/state`, 
            (state) => {
                console.log('Received initial game state:', state);
                dispatch({ type: 'SET_INITIAL_STATE', payload: state });
            }
        );

        const updatesSubscription = subscribe<GameUpdatePayload<any>>(
            `/topic/session/${sessionId}/game-updates`, 
            (update) => {
                console.log(`%c[CLIENT] Received event: ${update.actionType}`, 'color: purple; font-weight: bold;', update.payload);

                // Если это событие MOVE, логируем дополнительную информацию
                if (update.actionType === 'entity_moved') {
                    const payload = update.payload as EntityMovedEvent;
                    console.log(`%c    Moved entity ${payload.entityId} to new position: (${payload.newPosition.q},${payload.newPosition.r})`, 'color: #2c3e50;');
                }

                // Если это событие COMBAT_STARTED, просто логируем факт
                if (update.actionType === 'combat_started') {
                    const payload = update.payload as CombatStartedEvent;
                    console.log(`%c    Combat has started! ID: ${payload.combatId}. Initial turn order:`, 'color: red; font-weight: bold;', payload.initialTurnOrder);
                }
                setErrorMessage('');

                switch (update.actionType) {
                    case 'entity_moved':
                        dispatch({ type: 'ENTITY_MOVED', payload: update.payload as EntityMovedEvent });
                        break;
                    case 'entity_attack':
                        dispatch({ type: 'ENTITY_ATTACKED', payload: update.payload as EntityAttackEvent });
                        break;
                    case 'entity_stats_updated':
                        dispatch({ type: 'ENTITY_TOOK_DAMAGE', payload: update.payload as EntityStatsUpdatedEvent });
                        break;
                    case 'player_joined':
                        dispatch({ type: 'ADD_NEW_ENTITY', payload: update.payload as PlayerStateDto });
                        break;
                    case 'player_left':
                        dispatch({ type: 'REMOVE_ENTITY', payload: update.payload as PlayerLeftEvent });
                        break;
                    case 'combat_started':
                        dispatch({ type: 'COMBAT_STARTED', payload: update.payload as CombatStartedEvent });
                        break;
                    case 'combat_next_turn':
                        dispatch({ type: 'NEXT_TURN', payload: update.payload as CombatNextTurnEvent });
                        break;
                    case 'combat_participants_joined':
                        dispatch({ type: 'COMBAT_PARTICIPANTS_JOINED', payload: update.payload as CombatParticipantsJoinedEvent });
                        break;
                    case 'combat_ended':
                        dispatch({ type: 'COMBAT_ENDED', payload: update.payload as CombatEndedEvent });
                        break;

                    // --- ДОБАВЛЕНО: Обработка новых событий ---
                    case 'caster_state_updated':
                        dispatch({ type: 'CASTER_STATE_UPDATED', payload: update.payload as CasterStateUpdatedEvent });
                        break;
                    
                    case 'ability_casted':
                        dispatch({ type: 'ABILITY_CASTED', payload: update.payload as AbilityCastedEvent });
                        break;
                }
            }
        );
        
        publish('/app/join-session', { sessionId: sessionId });

        return () => {
            initialStateSubscription?.unsubscribe();
            updatesSubscription?.unsubscribe();
        };

    }, [sessionId, isConnectedToServer, dispatch, setErrorMessage]);

    const handleCreateGame = () => {
        if (isConnectedToServer) {
            setErrorMessage('');
            publish('/app/create-session', {});
        } else {
            setErrorMessage('Not connected to the server.');
        }
    };

    const handleJoinGame = () => {
        if (isConnectedToServer && joinSessionId) {
            setErrorMessage('');
            setSessionId(joinSessionId);
        } else {
            setErrorMessage('Not connected or Session ID is empty.');
        }
    };

    return (
        <div style={{ 
            position: 'relative',
            padding: '20px', 
            fontFamily: 'system-ui, sans-serif', 
            maxWidth: '1240px',
            margin: '0 auto' 
        }}>
            <div style={{
                position: 'absolute',
                top: '10px',
                left: '50%',
                transform: 'translateX(-50%)',
                zIndex: 1000,
                width: '90%',
                maxWidth: '600px',
                opacity: errorMessage ? 1 : 0,
                visibility: errorMessage ? 'visible' : 'hidden',
                transition: 'opacity 0.3s ease-in-out, visibility 0.3s ease-in-out',
            }}>
                {errorMessage && (
                    <p style={{ 
                        color: 'white', 
                        backgroundColor: '#c82333', 
                        padding: '12px', 
                        borderRadius: '5px',
                        margin: 0,
                        boxShadow: '0 4px 8px rgba(0,0,0,0.2)',
                        textAlign: 'center',
                    }}>
                        <strong>Error:</strong> {errorMessage}
                    </p>
                )}
            </div>
            
            <header style={{ marginBottom: '20px', borderBottom: '1px solid #ccc', paddingBottom: '10px' }}>
                <h1>Dungeon Crawler Prototype</h1>
                <p style={{ margin: '8px 0' }}>
                    Connection Status: 
                    <span style={{ 
                        color: isConnectedToServer ? '#28a745' : '#dc3545', 
                        fontWeight: 'bold',
                        marginLeft: '8px' 
                    }}>
                        {isConnectedToServer ? 'Connected' : 'Disconnected'}
                    </span>
                </p>
            </header>

            <main>
                {!sessionId ? (
                    <div id="lobby">
                        <h2>Lobby</h2>
                        <p>Create a new game or join an existing one using a Session ID.</p>
                        
                        <div style={{ marginBottom: '20px', padding: '15px', border: '1px solid #ddd', borderRadius: '5px' }}>
                            <h3>Create a New Game</h3>
                            <button 
                                onClick={handleCreateGame} 
                                disabled={!isConnectedToServer}
                                style={{ padding: '10px 15px', fontSize: '16px', cursor: 'pointer', border: '1px solid #007bff', backgroundColor: '#007bff', color: 'white', borderRadius: '5px' }}
                            >
                                Create Game
                            </button>
                        </div>

                        <div style={{ padding: '15px', border: '1px solid #ddd', borderRadius: '5px' }}>
                            <h3>Join an Existing Game</h3>
                            <div style={{ display: 'flex', alignItems: 'center' }}>
                                <input
                                    type="text"
                                    placeholder="Enter Session ID to join"
                                    value={joinSessionId}
                                    onChange={(e) => setJoinSessionId(e.target.value)}
                                    style={{ padding: '10px', fontSize: '16px', marginRight: '10px', flexGrow: 1, border: '1px solid #ccc', borderRadius: '5px' }}
                                />
                                <button 
                                    onClick={handleJoinGame} 
                                    disabled={!isConnectedToServer || !joinSessionId}
                                    style={{ padding: '10px 15px', fontSize: '16px', cursor: 'pointer', border: '1px solid #28a745', backgroundColor: '#28a745', color: 'white', borderRadius: '5px' }}
                                >
                                    Join Game
                                </button>
                            </div>
                        </div>
                    </div>
                ) : (
                    <div id="game-session">
                        <h2 style={{ wordBreak: 'break-all' }}>
                            Game Session ID: 
                            <span style={{ fontFamily: 'monospace', backgroundColor: '#eee', padding: '2px 6px', borderRadius: '3px', marginLeft: '8px' }}>
                                {sessionId}
                            </span>
                        </h2>
                        
                        {gameState.mapState && gameState.mapState.tiles.length > 0 ? (
                            <>
                                <div style={{ 
                                    position: 'relative',
                                    border: '2px solid black', 
                                    display: 'inline-block', 
                                    marginTop: '10px',
                                    lineHeight: 0,
                                    backgroundColor: '#1d2327' 
                                }}>
                                    <GameCanvas />
                                    <NotificationUI />
                                    <TurnOrder />
                                    <CombatOutcomeNotification />
                                    <AbilityBar />
                                    <MainHUD />
                                    <PeaceProposalUI />
                                </div>
                                <p>Click on the map to move your character. Click on another character to attack.</p>
                            </>
                        ) : (
                            <div style={{ marginTop: '20px', padding: '15px', border: '1px solid #ddd', borderRadius: '5px' }}>
                                <p style={{ margin: 0, fontWeight: 'bold' }}>Connecting to session and loading map...</p>
                            </div>
                        )}
                    </div>
                )}
            </main>
        </div>
    );
};

const App: FC = () => {
    return (
        <GameProvider>
            <Game />
        </GameProvider>
    );
};

export default App;