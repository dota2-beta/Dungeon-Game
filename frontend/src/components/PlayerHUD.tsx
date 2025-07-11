import React, { useState, useEffect, useRef } from 'react';
import { useGame } from '../context/GameContext';
import type { PlayerState } from '../types/dto';

/**
 * Компонент для отображения полоски здоровья.
 * Принимает флаг isDamaged для временного изменения цвета.
 */
const HealthBar: React.FC<{ current: number; max: number; isDamaged: boolean }> = ({ current, max, isDamaged }) => {
    const percentage = max > 0 ? (current / max) * 100 : 0;
    
    const barColor = isDamaged 
        ? '#ff4444' 
        : percentage > 50 ? '#4CAF50' : percentage > 25 ? '#FFC107' : '#F44336';

    return (
        <div style={{
            width: '100%',
            height: '20px',
            backgroundColor: '#555',
            borderRadius: '5px',
            overflow: 'hidden',
            border: '1px solid #222',
        }}>
            <div style={{
                width: `${percentage}%`,
                height: '100%',
                backgroundColor: barColor,
                transition: 'width 0.3s ease-in-out, background-color 0.1s ease-in-out',
            }} />
        </div>
    );
};

/**
 * Основной компонент интерфейса игрока (HUD).
 */
const PlayerHUD: React.FC = () => {
    const { gameState } = useGame();
    const [isDamaged, setIsDamaged] = useState(false);
    
    const lastProcessedTimestamp = useRef<number | null>(null);

    const player = gameState.entities.find(e => e.id === gameState.yourPlayerId) as PlayerState | undefined;
    const lastDamageInfo = gameState.lastDamage;

    useEffect(() => {
        if (!lastDamageInfo || !player) return;

        const { payload, timestamp } = lastDamageInfo;

        if (payload.targetEntityId === player.id && timestamp !== lastProcessedTimestamp.current) {
            lastProcessedTimestamp.current = timestamp;
            setIsDamaged(true);
        }
    }, [lastDamageInfo, player]);

    useEffect(() => {
        if (isDamaged) {
            const timer = setTimeout(() => {
                setIsDamaged(false);
            }, 500);

            return () => clearTimeout(timer);
        }
    }, [isDamaged]);

    if (!player) {
        return null;
    }

    if (player.isDead) {
        return (
            <div style={{
                position: 'absolute',
                bottom: '20px',
                left: '20px',
                width: '250px',
                padding: '15px',
                backgroundColor: 'rgba(50, 20, 20, 0.8)',
                color: '#aaa',
                borderRadius: '10px',
                border: '1px solid rgba(255, 0, 0, 0.4)',
                fontFamily: 'system-ui, sans-serif',
                boxShadow: '0 4px 12px rgba(0,0,0,0.5)',
                zIndex: 100,
                textAlign: 'center'
            }}>
                <h2 style={{ color: '#ff4444', margin: 0, textTransform: 'uppercase' }}>You are dead</h2>
            </div>
        );
    }

    const hpTextStyle: React.CSSProperties = {
        color: isDamaged ? '#ff4444' : 'white',
        fontWeight: isDamaged ? 'bold' : 'normal',
        transition: 'color 0.1s ease-in-out',
        textShadow: isDamaged ? '0 0 8px rgba(255, 0, 0, 0.8)' : 'none'
    };

    return (
        <div style={{
            position: 'absolute',
            bottom: '20px',
            left: '20px',
            width: '250px',
            padding: '15px',
            backgroundColor: 'rgba(0, 0, 0, 0.7)',
            color: 'white',
            borderRadius: '10px',
            border: '1px solid rgba(255, 255, 255, 0.2)',
            fontFamily: 'system-ui, sans-serif',
            boxShadow: '0 4px 12px rgba(0,0,0,0.5)',
            zIndex: 100
        }}>
            <h3 style={{ marginTop: 0, marginBottom: '10px', borderBottom: '1px solid #444', paddingBottom: '5px' }}>
                {player.name}
            </h3>
            <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '5px' }}>
                    <span>HP</span>
                    <span style={hpTextStyle}>
                        {player.currentHp} / {player.maxHp}
                    </span>
                </div>
                <HealthBar current={player.currentHp} max={player.maxHp} isDamaged={isDamaged} />
            </div>
        </div>
    );
};

export default PlayerHUD;