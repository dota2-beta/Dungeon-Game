import React from 'react';
import { useGame } from '../context/GameContext';
import type { EntityStateDto } from '../types/dto';

const TurnOrder: React.FC = () => {
    const { gameState } = useGame();
    const { activeCombat, entities, yourPlayerId } = gameState;

    if (!activeCombat) {
        return null;
    }

    const me = entities.find(e => e.id === yourPlayerId);
    const currentTurnEntity = entities.find(e => e.id === activeCombat.currentTurnEntityId);

    const isMyTurn = activeCombat.currentTurnEntityId === yourPlayerId;
    
    const isAllyTurn = !isMyTurn && me?.teamId != null && me.teamId === currentTurnEntity?.teamId;

    let turnText: string;
    let turnBackgroundColor: string;

    if (isMyTurn) {
        turnText = 'Your Turn';
        turnBackgroundColor = 'rgba(0, 150, 255, 0.8)';
    } else if (isAllyTurn) {
        turnText = `Ally Turn: ${currentTurnEntity?.name || 'Ally'}`;
        turnBackgroundColor = 'rgba(0, 128, 0, 0.8)';
    } else {
        turnText = 'Enemy Turn';
        turnBackgroundColor = 'rgba(200, 0, 0, 0.8)';
    }
    

    const turnOrderEntities: (EntityStateDto | undefined)[] = activeCombat.turnOrder.map(entityId =>
        entities.find(e => e.id === entityId)
    );
    
    return (
        <div style={{
            position: 'absolute',
            top: '20px',
            left: '50%',
            transform: 'translateX(-50%)',
            zIndex: 110,
            textAlign: 'center',
        }}>
            <div style={{
                padding: '8px 24px',
                backgroundColor: turnBackgroundColor,
                color: 'white',
                borderRadius: '8px',
                border: '2px solid rgba(255, 255, 255, 0.5)',
                marginBottom: '10px',
                fontSize: '20px',
                fontWeight: 'bold',
                textTransform: 'uppercase',
                boxShadow: '0 4px 8px rgba(0,0,0,0.5)',
            }}>
                {turnText}
            </div>
            
            <div style={{
                display: 'flex',
                gap: '4px',
                padding: '8px',
                backgroundColor: 'rgba(0, 0, 0, 0.7)',
                borderRadius: '8px',
            }}>
                {turnOrderEntities.map((entity) => {
                    if (!entity || entity.dead) return null;

                    const isCurrent = entity.id === activeCombat.currentTurnEntityId;
                    const isAllyPortrait = entity.id === yourPlayerId || 
                                           (me?.teamId != null && me.teamId === entity.teamId);

                    return (
                        <div key={entity.id} style={{
                            width: '50px',
                            height: '60px',
                            backgroundColor: isAllyPortrait ? '#2a3d4a' : '#5a2a2a',
                            border: `3px solid ${isCurrent ? '#ffcc00' : isAllyPortrait ? '#4a7d9a' : '#9a4a4a'}`,
                            borderRadius: '4px',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            color: 'white',
                            fontWeight: 'bold',
                            transition: 'all 0.3s ease',
                            transform: isCurrent ? 'scale(1.1)' : 'scale(1)',
                        }}>
                            {entity.name ? entity.name.charAt(0).toUpperCase() : '?'}
                        </div>
                    );
                })}
            </div>
        </div>
    );
};

export default TurnOrder;