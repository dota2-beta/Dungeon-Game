import React from 'react';
// Импортируй свои типы. Если EntityState лежит в другом месте - поправь путь
import { type  EntityState } from '../types/game'; 

// Определяем интерфейс для пропсов, основываясь на том, как выглядит твой entity
interface Props {
    entity: any | null; // Лучше заменить 'any' на твой тип Entity (например EntityData)
}

export const EntityStatusOverlay: React.FC<Props> = ({ entity }) => {
    if (!entity) return null;

    return (
        <div style={{
            position: 'absolute',
            top: '20px', // Отступ сверху
            left: '50%',
            transform: 'translateX(-50%)', // Строго по центру
            
            background: 'rgba(0, 0, 0, 0.85)', // Темная подложка
            backdropFilter: 'blur(4px)', // Размытие фона (красивый эффект)
            
            padding: '8px 25px',
            borderRadius: '12px',
            border: '1px solid #555',
            boxShadow: '0 4px 15px rgba(0,0,0,0.5)',
            
            color: '#fff',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            gap: '4px',
            zIndex: 1000,
            pointerEvents: 'none', // ВАЖНО: пропускаем клики сквозь плашку
            minWidth: '220px'
        }}>
            {/* Имя */}
            <div style={{ 
                color: '#FFD700', 
                fontWeight: 'bold', 
                fontSize: '16px', 
                textTransform: 'uppercase',
                letterSpacing: '1px'
            }}>
                {entity.name}
            </div>

            {/* Статы: HP и Armor */}
            <div style={{ display: 'flex', gap: '20px', fontSize: '14px', width: '100%', justifyContent: 'center' }}>
                <span style={{ color: '#ff5252', fontWeight: 'bold' }}>
                    ❤️ {entity.currentHp} / {entity.maxHp}
                </span>
                <span style={{ color: '#90caf9', fontWeight: 'bold' }}>
                    🛡️ {entity.defense}
                </span>
            </div>

            {/* Полоска здоровья (визуальная) */}
            <div style={{ width: '100%', height: '4px', background: '#333', borderRadius: '2px', marginTop: '4px' }}>
                <div style={{
                    width: `${Math.max(0, (entity.currentHp / entity.maxHp) * 100)}%`,
                    height: '100%',
                    background: '#ff5252',
                    borderRadius: '2px',
                    transition: 'width 0.2s ease-out'
                }} />
            </div>
        </div>
    );
};