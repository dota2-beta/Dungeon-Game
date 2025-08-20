import React, { useEffect } from 'react';
import { useGame } from '../context/GameContext';
import type { CombatOutcome } from '../types/dto';

const CombatOutcomeNotification: React.FC = () => {
    const { gameState, dispatch } = useGame();
    const { combatOutcomeInfo } = gameState;

    useEffect(() => {
        if (combatOutcomeInfo) {
            const timer = setTimeout(() => {
                dispatch({ type: 'CLEAR_COMBAT_OUTCOME' });
            }, 4000);

            return () => clearTimeout(timer);
        }
    }, [combatOutcomeInfo, dispatch]);

    if (!combatOutcomeInfo) {
        return null;
    }

    
    const getColorForOutcome = (outcome: CombatOutcome) => {
        switch (outcome) {
            case 'VICTORY': return '#4CAF50';
            case 'DEFEAT': return '#F44336';
            case 'END_BY_AGREEMENT': return '#3498db';
            default: return '#95a5a6';
        }
    };

    const color = getColorForOutcome(combatOutcomeInfo.outcome);

    const style: React.CSSProperties = {
        position: 'absolute',
        top: '30%',
        left: '50%',
        transform: 'translate(-50%, -50%)',
        padding: '20px 40px',
        backgroundColor: 'rgba(0, 0, 0, 0.8)',
        color: 'white',
        borderRadius: '10px',
        border: `2px solid ${color}`,
        fontSize: '48px',
        fontWeight: 'bold',
        textTransform: 'uppercase',
        zIndex: 150,
        textShadow: `0 0 15px ${color}`,
        boxShadow: '0 0 25px rgba(0,0,0,0.7)',
        animation: 'fadeInOut 4s ease-in-out'
    };

    return (
        <>
            <style>
                {`
                    @keyframes fadeInOut {
                        0% { opacity: 0; transform: translate(-50%, -50%) scale(0.8); }
                        15% { opacity: 1; transform: translate(-50%, -50%) scale(1); }
                        85% { opacity: 1; transform: translate(-50%, -50%) scale(1); }
                        100% { opacity: 0; transform: translate(-50%, -50%) scale(0.8); }
                    }
                `}
            </style>
            <div style={style}>
                {combatOutcomeInfo.message}
            </div>
        </>
    );
};

export default CombatOutcomeNotification;