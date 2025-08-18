import React from 'react';

const HealthBar: React.FC<{ current: number; max: number; isDamaged?: boolean }> = ({ current, max, isDamaged }) => {
    const percentage = max > 0 ? (current / max) * 100 : 0;
    const barColor = isDamaged ? '#ff4444' : percentage > 50 ? '#4CAF50' : percentage > 25 ? '#FFC107' : '#F44336';
    
    return (
        <div style={{ width: '100%', height: '100%', backgroundColor: '#111' }}>
            <div style={{ width: `${percentage}%`, height: '100%', backgroundColor: barColor, transition: 'width 0.3s ease-in-out, background-color 0.1s ease-in-out' }} />
        </div>
    );
};

export default HealthBar;