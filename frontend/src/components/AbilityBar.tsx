import React, { useState, useEffect } from 'react';
import { useGame } from '../context/GameContext';
import { type AbilityStateDto, EntityStateType } from '../types/dto';
const AbilitySlot: React.FC<{ ability: AbilityStateDto }> = ({ ability }) => {
    const { gameState, dispatch } = useGame();
    const [cooldownText, setCooldownText] = useState('');

    const template = gameState.abilities.find(
        (a) => a.templateId === ability.abilityTemplateId
    );

    const player = gameState.entities.find(e => e.id === gameState.yourPlayerId);
    const isInCombat = player?.state === EntityStateType.COMBAT;
    const isMyTurn = gameState.activeCombat?.currentTurnEntityId === player?.id;

    useEffect(() => {
        const updateCooldownDisplay = () => {
            if (isInCombat) {
                setCooldownText(ability.turnCooldown > 0 ? `${ability.turnCooldown}` : '');
            } else {
                const now = Date.now();
                if (now < ability.cooldownEndTime) {
                    const secondsLeft = Math.ceil((ability.cooldownEndTime - now) / 1000);
                    setCooldownText(`${secondsLeft}s`);
                } else {
                    setCooldownText('');
                }
            }
        };

        updateCooldownDisplay(); 
        if (!isInCombat && Date.now() < ability.cooldownEndTime) {
            const interval = setInterval(updateCooldownDisplay, 1000);
            return () => clearInterval(interval);
        }
    }, [ability, isInCombat]);

    const isReady = isInCombat ? ability.turnCooldown <= 0 : Date.now() >= ability.cooldownEndTime;
    const isDisabled = (isInCombat && !isMyTurn) || !isReady;

    const isSelected = gameState.selectedAbility?.abilityTemplateId === ability.abilityTemplateId;
    const handleClick = () => {
        if (isDisabled) return;
        dispatch({ type: 'SELECT_ABILITY', payload: ability });
    };
    const displayName = template?.name || ability.abilityTemplateId;
    const displayInitial = template ? template.name.charAt(0).toUpperCase() : '?';

    return (
        <button
            onClick={handleClick}
            disabled={isDisabled}
            title={displayName}
            style={{
                width: '64px',
                height: '64px',
                backgroundColor: '#2d3436',
                border: `3px solid ${isSelected ? '#74b9ff' : (isMyTurn && isReady ? '#f1c40f' : '#636e72')}`,
                transform: isSelected ? 'scale(1.1)' : 'scale(1)',
                boxShadow: isSelected ? '0 0 15px #0984e3' : 'none',
                borderRadius: '8px',
                color: 'white',
                fontSize: '12px',
                fontWeight: 'bold',
                cursor: isDisabled ? 'not-allowed' : 'pointer',
                position: 'relative',
                opacity: isDisabled && !isMyTurn ? 0.5 : 1,
                transition: 'all 0.2s ease',
                overflow: 'hidden',
                padding: '4px',
                textAlign: 'center',
                textTransform: 'capitalize',
            }}
        >
            {displayInitial}
            {ability.abilityTemplateId?.split('_')[0] || '?'}
            {cooldownText && (
                <div style={{
                    position: 'absolute',
                    top: 0, left: 0, right: 0, bottom: 0,
                    backgroundColor: 'rgba(0, 0, 0, 0.75)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontSize: '24px',
                    borderRadius: '5px', 
                }}>
                    {cooldownText}
                </div>
            )}
        </button>
    );
};


/**
 * Основной компонент-контейнер для панели способностей.
 */
const AbilityBar: React.FC = () => {
    const { gameState } = useGame();
    const player = gameState.entities.find(e => e.id === gameState.yourPlayerId);
    if (!player || !player.abilities || player.abilities.length === 0) {
        return null;
    }

    return (
        <div style={{
            position: 'absolute',
            bottom: '110px',
            left: '50%',
            transform: 'translateX(-50%)',
            zIndex: 120,
            display: 'flex',
            gap: '8px',
            padding: '8px',
            backgroundColor: 'rgba(0, 0, 0, 0.8)',
            borderRadius: '12px',
            border: '1px solid rgba(255, 255, 255, 0.2)',
            boxShadow: '0 4px 12px rgba(0,0,0,0.5)',
        }}>
            {player.abilities.map(ability => (
                <AbilitySlot key={ability.abilityTemplateId} ability={ability} />
            ))}
        </div>
    );
};

export default AbilityBar;