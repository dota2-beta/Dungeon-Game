import React from 'react';
import { useGameState } from '../store/GameContext';
import { gameSocket } from '../api/GameSocket';
import { EntityState } from '../types/game';
// 1. Импортируем наш новый компонент
import { EntityStatusOverlay } from './EntityStatusOverlay';

export const GameInterface: React.FC = () => {
    const { state } = useGameState();
    const { entities, userId, sessionId, activeEntityId, hoveredEntityId, turnOrder } = state;

    // Находим себя
    const myPlayer = Object.values(entities).find(e => e.userId === userId);
    
    // 2. На кого смотрим мышкой (эта логика у тебя уже была, она правильная)
    const targetEntity = hoveredEntityId ? entities[hoveredEntityId] : null;
    
    // Проверки состояния
    const inCombat = myPlayer?.state === EntityState.COMBAT;
    const isMyTurn = inCombat && activeEntityId === myPlayer?.id;

    // --- 1. ОЧЕРЕДЬ ХОДОВ (Сверху) ---
    const TurnOrderBar = () => {
        if (!inCombat || !turnOrder || turnOrder.length === 0) return null;

        return (
            <div style={{
                position: 'absolute', top: 80, // Чуть опустим, чтобы не перекрывать статус
                left: '50%', transform: 'translateX(-50%)',
                display: 'flex', gap: 8, background: 'rgba(0,0,0,0.7)', padding: '8px 15px', 
                borderRadius: 30, border: '1px solid #444', pointerEvents: 'auto'
            }}>
                {turnOrder.map((id, index) => {
                    const ent = entities[id];
                    if (!ent) return null; 
                    
                    const isActive = id === activeEntityId;
                    const isDead = ent.currentHp <= 0;

                    let bgColor = '#F44336';
                    if (ent.type === 'PLAYER') {
                        bgColor = (ent.userId === userId) ? '#4CAF50' : '#2196F3';
                    }

                    return (
                        <div key={`${id}_${index}`} style={{
                            width: 45, height: 45, 
                            borderRadius: '50%', 
                            border: isActive ? '3px solid #FFD700' : '2px solid #555',
                            background: bgColor,
                            display: 'flex', alignItems: 'center', justifyContent: 'center',
                            color: 'white', fontSize: 12, fontWeight: 'bold',
                            opacity: isDead ? 0.5 : 1,
                            transform: isActive ? 'scale(1.2)' : 'scale(1)',
                            transition: 'all 0.3s ease',
                            position: 'relative'
                        }} title={ent.name}>
                            {ent.name.substring(0, 2).toUpperCase()}
                            <div style={{
                                position: 'absolute', bottom: -2, right: -2,
                                fontSize: 9, background: '#333', padding: '1px 4px', borderRadius: 4
                            }}>
                                {ent.currentHp}
                            </div>
                        </div>
                    );
                })}
            </div>
        );
    };

    const PeaceModal = () => {
        if (!state.peaceProposal) return null;
        const isInitiator = myPlayer?.id === state.peaceProposal.initiatorId;

        return (
            <div style={{
                position: 'absolute', top: '30%', left: '50%', transform: 'translate(-50%, -50%)',
                background: 'rgba(0, 0, 0, 0.95)', padding: '20px 40px', borderRadius: 12,
                border: '2px solid #FFD700', textAlign: 'center', pointerEvents: 'auto', zIndex: 2000,
                boxShadow: '0 0 50px rgba(255, 215, 0, 0.2)', minWidth: 300
            }}>
                <div style={{fontSize: 40, marginBottom: 10}}>🏳️</div>
                <h2 style={{color: '#FFD700', margin: '0 0 10px 0', textTransform: 'uppercase'}}>Peace Proposal</h2>
                <p style={{color: '#eee', fontSize: 16, marginBottom: 20}}>
                    {isInitiator ? (
                        <span>You proposed peace. Waiting for response...</span>
                    ) : (
                        <span><span style={{fontWeight: 'bold', color: '#4CAF50'}}>{state.peaceProposal.initiatorName}</span> wants to stop the fight.</span>
                    )}
                </p>
                {!isInitiator && (
                    <div style={{display: 'flex', gap: 20, justifyContent: 'center'}}>
                        <button onClick={() => gameSocket.respondToPeace(sessionId!, true)} style={{background: '#2E7D32', color: 'white', border: 'none', padding: '10px 30px', borderRadius: 6, cursor: 'pointer', fontWeight: 'bold', fontSize: 16}}>ACCEPT</button>
                        <button onClick={() => gameSocket.respondToPeace(sessionId!, false)} style={{background: '#C62828', color: 'white', border: 'none', padding: '10px 30px', borderRadius: 6, cursor: 'pointer', fontWeight: 'bold', fontSize: 16}}>REJECT</button>
                    </div>
                )}
                {isInitiator && <div style={{color: '#aaa', fontSize: 12}}>⏳ Poll in progress...</div>}
            </div>
        );
    };

    // --- 3. ПАНЕЛЬ ИГРОКА (Снизу) ---
    const BottomPanel = () => {
        if (!myPlayer) return null;
        return (
            <div style={{
                position: 'absolute', bottom: 30, left: '50%', transform: 'translateX(-50%)',
                display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 12,
                pointerEvents: 'auto', zIndex: 100
            }}>
                {inCombat && (
                    <div style={{ display: 'flex', gap: 6 }}>
                        {Array.from({ length: myPlayer.maxAP }).map((_, i) => {
                            const hasPoint = i < myPlayer.currentAP;
                            return (
                                <div key={i} style={{
                                    width: 18, height: 18, borderRadius: '50%',
                                    background: hasPoint ? '#76FF03' : '#424242',
                                    border: '1px solid #fff',
                                    boxShadow: hasPoint ? '0 0 8px #76FF03' : 'none',
                                    transition: 'background 0.3s'
                                }} />
                            );
                        })}
                    </div>
                )}
                <div style={{
                    background: '#1e1e1e', padding: '10px 20px', borderRadius: 12, border: '2px solid #555',
                    display: 'flex', gap: 25, minWidth: 300, justifyContent: 'center',
                    boxShadow: '0 5px 20px rgba(0,0,0,0.5)'
                }}>
                    <div style={{ flex: 1 }}>
                        <div style={{color: '#ff5252', fontWeight: 'bold', fontSize: 12, marginBottom: 2}}>
                            HP {myPlayer.currentHp} / {myPlayer.maxHp}
                        </div>
                        <div style={{width: '100%', height: 8, background: '#3e1b1b', borderRadius: 4, overflow: 'hidden'}}>
                            <div style={{
                                width: `${Math.max(0, (myPlayer.currentHp / myPlayer.maxHp) * 100)}%`, 
                                height: '100%', background: '#ff5252', transition: 'width 0.3s'
                            }}/>
                        </div>
                    </div>
                    <div style={{ width: 80 }}>
                        <div style={{color: '#b0bec5', fontWeight: 'bold', fontSize: 12, marginBottom: 2}}>
                            DEF {myPlayer.defense}
                        </div>
                        <div style={{width: '100%', height: 8, background: '#263238', borderRadius: 4, overflow: 'hidden'}}>
                            <div style={{width: '100%', height: '100%', background: '#78909c'}}/>
                        </div>
                    </div>
                </div>
                
                {/* Actions */}
                <div style={{display: 'flex', gap: 15, alignItems: 'center', marginTop: 5}}>
                    {inCombat && (
                        <button onClick={() => gameSocket.proposePeace(sessionId!)} title="Propose Peace" style={{width: 45, height: 45, borderRadius: '50%', border: '2px solid #fff', background: '#fff', color: '#000', fontSize: 24, cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center'}}>🏳️</button>
                    )}
                    {inCombat && (
                        <button onClick={() => gameSocket.endTurn(sessionId!)} disabled={!isMyTurn} style={{padding: '12px 40px', background: isMyTurn ? 'linear-gradient(45deg, #FF6F00, #FFCA28)' : '#333', color: isMyTurn ? 'black' : '#777', border: isMyTurn ? '1px solid #FFD54F' : '1px solid #555', borderRadius: 6, fontWeight: 'bold', fontSize: 16, cursor: isMyTurn ? 'pointer' : 'not-allowed', boxShadow: isMyTurn ? '0 0 15px rgba(255, 160, 0, 0.6)' : 'none', transition: 'all 0.3s'}}>
                            {isMyTurn ? "END TURN SPACE" : "WAITING..."}
                        </button>
                    )}
                </div>
            </div>
        );
    };

    return (
        <div style={{ 
            position: 'absolute', top: 0, left: 0, width: '100%', height: '100%', 
            pointerEvents: 'none' 
        }}>
            {/* 3. Рендерим наш новый компонент статуса */}
            <EntityStatusOverlay entity={targetEntity} />

            <PeaceModal />
            <div style={{pointerEvents: 'auto'}}><TurnOrderBar /></div>
            <div style={{pointerEvents: 'auto'}}><BottomPanel /></div>
        </div>
    );
};