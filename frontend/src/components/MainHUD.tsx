import React from 'react';
import PlayerHUD from './PlayerHUD';
import ActionBar from './ActionBar';
import { useGame } from '../context/GameContext';

const MainHUD: React.FC = () => {
    const { gameState } = useGame();
    const { activeCombat } = gameState;

    return (
        <div style={{
            position: 'absolute',
            bottom: '20px',
            left: '50%',
            transform: 'translateX(-50%)',
            zIndex: 110,
            display: 'flex',
            alignItems: 'flex-end', 
            gap: '16px',
        }}>
            {activeCombat && <ActionBar />}
            <PlayerHUD />
        </div>
    );
};

export default MainHUD;