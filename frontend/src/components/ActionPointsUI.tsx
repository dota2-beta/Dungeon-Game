import React from 'react';

interface ActionPointsUIProps {
    current: number;
    max: number;
}

const ActionPointOrb: React.FC<{ active: boolean }> = ({ active }) => {
    const orbStyle: React.CSSProperties = {
        width: '28px',
        height: '28px',
        borderRadius: '50%',
        transition: 'all 0.3s ease',
        position: 'relative',
        boxShadow: active
            ? '0 0 10px rgba(76, 175, 80, 0.9), inset 0 0 5px rgba(255, 255, 255, 0.4)'
            : 'inset 0 0 8px rgba(0, 0, 0, 0.6)',
        background: active
            ? 'radial-gradient(circle at 30% 30%, #a5d6a7, #4CAF50)' 
            : 'radial-gradient(circle at 30% 30%, #616161, #212121)',
    };

    return <div style={orbStyle} />;
};

const ActionPointsUI: React.FC<ActionPointsUIProps> = ({ current, max }) => {
    const points = Array.from({ length: max }, (_, i) => (
        <ActionPointOrb key={i} active={i < current} />
    ));

    return (
        <div style={{
            display: 'flex',
            alignItems: 'center',
            gap: '6px',
            padding: '4px 8px',
            backgroundColor: 'rgba(0,0,0,0.5)',
            borderRadius: '16px',
            border: '1px solid rgba(255,255,255,0.1)',
        }}>
            {points}
        </div>
    );
};

export default ActionPointsUI;