import React from 'react';
import { publish } from '../api/websocketService';
import { useGame } from '../context/GameContext';

const TeamStatusUI: React.FC = () => {
    const { gameState } = useGame();
    const { entities, yourPlayerId, sessionId } = gameState;

    const player = entities.find(e => e.id === yourPlayerId);
    if (!player || !player.teamId) {
        return null;
    }
    
    const allTeamMembers = entities.filter(e => e.teamId === player.teamId);

    if (allTeamMembers.length <= 1) {
        return null;
    }
    
    const teammates = allTeamMembers.filter(e => e.id !== yourPlayerId);

    const handleLeaveTeam = () => {
        //console.log("Sending request to leave team...");
        publish(`/app/session/${sessionId}/team/leave`, {});
    };

    return (
        <div style={{
            position: 'absolute',
            top: '20px',
            right: '20px',
            zIndex: 110,
            backgroundColor: 'rgba(20, 20, 20, 0.85)',
            borderRadius: '10px',
            border: '1px solid rgba(255, 255, 255, 0.2)',
            boxShadow: '0 4px 12px rgba(0,0,0,0.5)',
            color: 'white',
            fontFamily: 'system-ui, sans-serif',
            width: '220px',
            display: 'flex',
            flexDirection: 'column',
            maxHeight: '250px',
        }}>
            <h4 style={{
                margin: 0,
                padding: '10px 15px',
                borderBottom: '1px solid #444',
                flexShrink: 0,
            }}>
                Your Team
            </h4>
            <ul style={{
                listStyle: 'none',
                padding: '5px 0',
                margin: '0',
                overflowY: 'auto',
                flexGrow: 1,
            }}>
                <li style={{ padding: '8px 15px', lineHeight: '1.2' }}>
                    &#9733; {player.name} (You)
                </li>
                {teammates.map(mate => (
                    <li key={mate.id} style={{ padding: '8px 15px', lineHeight: '1.2' }}>
                        &#128100; {mate.name}
                    </li>
                ))}
            </ul>
            <button
                onClick={handleLeaveTeam}
                style={{
                    width: 'calc(100% - 20px)',
                    margin: '10px',
                    padding: '8px',
                    fontSize: '14px',
                    fontWeight: 'bold',
                    color: 'white',
                    backgroundColor: '#c0392b',
                    border: '1px solid #a03023',
                    borderRadius: '5px',
                    cursor: 'pointer',
                    flexShrink: 0,
                }}
            >
                Leave Team
            </button>
        </div>
    );
};

export default TeamStatusUI;