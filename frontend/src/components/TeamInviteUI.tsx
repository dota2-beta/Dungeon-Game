import React from 'react';
import { useGame } from '../context/GameContext';
import { publish } from '../api/websocketService';
import type { RespondToTeamRequest } from '../types/dto';

const TeamInviteUI: React.FC = () => {
    const { gameState, dispatch } = useGame();
    const { activeTeamInvite, sessionId } = gameState;

    if (!activeTeamInvite) {
        return null;
    }

    const handleResponse = (accepted: boolean) => {
        // БЫЛО:
        // const payload: RespondToTeamInviteRequest = { accepted };
        
        // СТАЛО:
        const payload: RespondToTeamRequest = { accepted };
        
        // ВАЖНО: Путь для отправки тоже должен соответствовать вашему контроллеру.
        // Я предполагаю, что он выглядит так. Если он другой, замените его.
        publish(`/app/session/${sessionId}/team/respond`, payload);
        
        dispatch({ type: 'CLEAR_TEAM_INVITE' });
    };

    const buttonStyle: React.CSSProperties = {
        padding: '10px 20px',
        fontSize: '16px',
        color: 'white',
        border: 'none',
        borderRadius: '5px',
        cursor: 'pointer',
        fontWeight: 'bold',
        minWidth: '100px',
    };

    return (
        <div style={{
            position: 'absolute',
            top: '220px',
            left: '50%',
            transform: 'translateX(-50%)',
            zIndex: 130,
            padding: '15px 25px',
            backgroundColor: 'rgba(45, 52, 54, 0.9)',
            borderRadius: '10px',
            border: '2px solid #a29bfe',
            color: 'white',
            textAlign: 'center',
            boxShadow: '0 5px 15px rgba(0,0,0,0.6)',
        }}>
            <h4 style={{ margin: 0, marginBottom: '15px', color: '#a29bfe', textTransform: 'uppercase' }}>
                Team Invitation
            </h4>
            <p style={{ margin: 0, marginBottom: '20px', fontSize: '18px' }}>
                Player <strong>{activeTeamInvite.fromPlayerName}</strong> invites you to join their team.
            </p>
            
            <div style={{ display: 'flex', gap: '15px', justifyContent: 'center' }}>
                <button onClick={() => handleResponse(true)} style={{ ...buttonStyle, backgroundColor: '#27ae60' }}>
                    Accept
                </button>
                <button onClick={() => handleResponse(false)} style={{ ...buttonStyle, backgroundColor: '#c0392b' }}>
                    Decline
                </button>
            </div>
        </div>
    );
};

export default TeamInviteUI;