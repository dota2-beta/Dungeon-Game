import React, { useState, useEffect, useRef } from 'react';
import { useGame } from '../context/GameContext';
import type { PlayerStateDto } from '../types/dto';
import HealthBar from './HealthBar';
import ArmorBar from './ArmorBar';

const PlayerHUD: React.FC = () => {
    const { gameState } = useGame();
    const [isDamaged, setIsDamaged] = useState(false);
    const [isArmorHit, setIsArmorHit] = useState(false);
    
    // --- НОВОЕ: Сохраняем максимальное значение брони ---
    const [maxDefense, setMaxDefense] = useState<number>(0);

    const lastProcessedTimestamp = useRef<number | null>(null);

    const player = gameState.entities.find(e => e.id === gameState.yourPlayerId) as PlayerStateDto | undefined;
    const lastDamageInfo = gameState.lastDamage;
    
    // Эффект для сохранения максимальной брони при появлении игрока
    useEffect(() => {
        if (player && player.defense > maxDefense) {
            setMaxDefense(player.defense);
        }
    }, [player, maxDefense]);

    useEffect(() => {
        if (!lastDamageInfo || !player) return;
        const { payload, timestamp } = lastDamageInfo;

        if (payload.targetEntityId === player.id && timestamp !== lastProcessedTimestamp.current) {
            lastProcessedTimestamp.current = timestamp;
            
            if (payload.damageToHp && payload.damageToHp > 0) {
                setIsDamaged(true);
            }
            if (payload.absorbedByArmor && payload.absorbedByArmor > 0) {
                setIsArmorHit(true);
            }
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

    if (player.dead) {
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
            width: '300px',
            padding: '10px 15px',
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
                <div style={{ height: '20px', width: '100%', borderRadius: '5px', overflow: 'hidden', border: '1px solid #222' }}>
                    <HealthBar current={player.currentHp} max={player.maxHp} isDamaged={isDamaged} />
                </div>
            </div>
            {maxDefense > 0 && ( // Показываем блок, только если есть броня
                <div style={{ marginTop: '10px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '5px' }}>
                        <span>Armor</span>
                        <span style={{ color: isArmorHit ? '#FFFFFF' : '#3498db', transition: 'color 0.1s ease-in-out' }}>
                            {player.defense} / {maxDefense}
                        </span>
                    </div>
                    <div style={{ height: '20px', width: '100%', borderRadius: '5px', overflow: 'hidden', border: '1px solid #222' }}>
                        <ArmorBar current={player.defense} max={maxDefense} isHit={isArmorHit} />
                    </div>
                </div>
            )}
        </div>
    );
};

export default PlayerHUD;