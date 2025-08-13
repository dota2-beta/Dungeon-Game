import React from 'react';
import { useGame } from '../context/GameContext';
import { publish } from '../api/websocketService';
import type { RespondToPeaceRequest } from '../types/dto';

const PeaceProposalUI: React.FC = () => {
    const { gameState } = useGame();
    const { activePeaceProposal, yourPlayerId, sessionId, activeCombat } = gameState;

    // UI виден только если есть активное голосование в активном бою
    if (!activePeaceProposal || !activeCombat) {
        return null;
    }

    const isMyProposal = activePeaceProposal.initiatorId === yourPlayerId;

    const handleResponse = (accepted: boolean) => {
        const payload: RespondToPeaceRequest = { accepted };
        publish(`/app/session/${sessionId}/combat/${activeCombat.combatId}/respond-peace`, payload);
    };

    const buttonStyle: React.CSSProperties = {
        padding: '10px 20px',
        fontSize: '16px',
        color: 'white',
        border: 'none',
        borderRadius: '5px',
        cursor: 'pointer',
        fontWeight: 'bold',
    };

    return (
        <div style={{
            position: 'absolute',
            top: '150px',
            left: '50%',
            transform: 'translateX(-50%)',
            zIndex: 130,
            padding: '15px 25px',
            backgroundColor: 'rgba(45, 52, 54, 0.9)',
            borderRadius: '10px',
            border: '2px solid #fdcb6e',
            color: 'white',
            textAlign: 'center',
            boxShadow: '0 5px 15px rgba(0,0,0,0.6)',
            minWidth: '300px',
            maxWidth: '500px',
        }}>
            <h4 style={{ margin: 0, marginBottom: '15px', color: '#fdcb6e', textTransform: 'uppercase' }}>
                Peace Proposal
            </h4>
            <p style={{
                margin: 0,
                marginBottom: '20px',
                fontSize: '18px',
                whiteSpace: 'pre-wrap', // Оставляем для переносов \n
                wordBreak: 'break-word', // Добавляем принудительный перенос слов
            }}>
                {isMyProposal 
                    ? "You have proposed peace. Waiting for others..." 
                    : `Player '${activePeaceProposal.initiatorName}' proposes to end the combat!`
                }
            </p>
            {!isMyProposal && (
                <div style={{ display: 'flex', gap: '15px', justifyContent: 'center' }}>
                    <button onClick={() => handleResponse(true)} style={{ ...buttonStyle, backgroundColor: '#27ae60' }}>
                        Agree (✓)
                    </button>
                    <button onClick={() => handleResponse(false)} style={{ ...buttonStyle, backgroundColor: '#c0392b' }}>
                        Decline (✗)
                    </button>
                </div>
            )}
        </div>
    );
};

export default PeaceProposalUI;