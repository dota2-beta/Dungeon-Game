import React from 'react';

const ArmorBar: React.FC<{ current: number; max: number; isHit?: boolean }> = ({ current, max, isHit }) => {
    const percentage = max > 0 ? (current / max) * 100 : 0;
    const barColor = isHit ? '#FFFFFF' : '#3498db';
    
    // --- ИСПРАВЛЕНИЕ: ВОЗВРАЩАЕМ height: '100%' ---
    return (
        <div style={{ width: '100%', height: '100%', backgroundColor: '#111' }}>
            <div style={{ width: `${percentage}%`, height: '100%', background: `linear-gradient(to right, #2980b9, ${barColor})`, transition: 'width 0.3s ease-in-out, background 0.1s ease-in-out' }} />
        </div>
    );
};

export default ArmorBar;