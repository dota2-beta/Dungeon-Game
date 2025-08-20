import React, { useRef } from 'react';
import { useGame } from '../context/GameContext';
import ArmorBar from './ArmorBar';
import HealthBar from './HealthBar';

const HoveredEntityHUD: React.FC = () => {
    const { gameState } = useGame();
    const { entities, hoveredEntityId } = gameState;

    const maxDefenseRef = useRef<Map<string, number>>(new Map());

    if (!hoveredEntityId) {
        return null;
    }

    const entity = entities.find(e => e.id === hoveredEntityId);

    if (!entity) {
        return null;
    }

    const knownMaxDefense = maxDefenseRef.current.get(entity.id) || 0;
    
    if (entity.defense > knownMaxDefense) {
        maxDefenseRef.current.set(entity.id, entity.defense);
    }
    
    const maxDefense = Math.max(knownMaxDefense, entity.defense);

    return (
        <div style={{
            position: 'absolute',
            top: '20px',
            left: '50%',
            transform: 'translateX(-50%)',
            zIndex: 150,
            width: '350px',
            backgroundColor: 'rgba(20, 20, 20, 0.8)',
            border: '1px solid #555',
            borderRadius: '4px',
            color: 'white',
            fontFamily: 'system-ui, sans-serif',
            padding: '8px 12px',
            pointerEvents: 'none',
        }}>
            <h4 style={{ margin: 0, textAlign: 'center', fontSize: '18px' }}>{entity.name}</h4>
            
            <div style={{ position: 'relative', height: '18px', margin: '8px 0', border: '1px solid #000' }}>
                <HealthBar current={entity.currentHp} max={entity.maxHp} />
                <span style={{ position: 'absolute', left: '50%', top: '50%', transform: 'translate(-50%, -50%)', fontSize: '14px', textShadow: '1px 1px 2px black' }}>
                    {entity.currentHp}/{entity.maxHp}
                </span>
            </div>
            {maxDefense > 0 && (
                <div style={{ position: 'relative', height: '18px', border: '1px solid #000' }}>
                    <ArmorBar current={entity.defense} max={maxDefense} />
                    <span style={{ position: 'absolute', left: '50%', top: '50%', transform: 'translate(-50%, -50%)', fontSize: '14px', textShadow: '1px 1px 2px black' }}>
                        {entity.defense}/{maxDefense}
                    </span>
                </div>
            )}
        </div>
    );
};

export default HoveredEntityHUD;