import React from 'react';
import { useGame } from '../context/GameContext';
import { publish } from '../api/websocketService';
import ActionPointsUI from './ActionPointsUI';

const ActionBar: React.FC = () => {
    const { gameState } = useGame();
    const { activeCombat, yourPlayerId, sessionId } = gameState;

    const player = gameState.entities.find(e => e.id === yourPlayerId);

    if (!activeCombat || !player) {
        return null;
    }

    console.log("PLAYER STATE FOR ACTION BAR:", player); 

    const isMyTurn = activeCombat.currentTurnEntityId === yourPlayerId;

    const handleEndTurn = () => {
        if (isMyTurn) {
            publish(`/app/session/${sessionId}/action`, { actionType: 'END_TURN' });
        }
    };


    return (
        <div style={{
            display: 'flex',
            alignItems: 'center',
            gap: '20px',
            padding: '10px 20px',
            backgroundColor: 'rgba(20, 20, 20, 0.85)',
            borderRadius: '10px',
            border: '1px solid rgba(255, 255, 255, 0.2)',
            boxShadow: '0 4px 12px rgba(0,0,0,0.5)',
        }}>
            <ActionPointsUI current={player.currentAP} max={player.maxAP} />
            
            <button
                onClick={handleEndTurn}
                disabled={!isMyTurn}
                style={{
                    padding: '10px 20px',
                    fontSize: '16px',
                    fontWeight: 'bold',
                    color: 'white',
                    backgroundColor: '#007bff',
                    border: '1px solid #0056b3',
                    borderRadius: '5px',
                    cursor: isMyTurn ? 'pointer' : 'not-allowed',
                    opacity: isMyTurn ? 1 : 0.5,
                    transition: 'opacity 0.2s, background-color 0.2s',
                }}
            >
                End Turn
            </button>
        </div>
    );
};

export default ActionBar;