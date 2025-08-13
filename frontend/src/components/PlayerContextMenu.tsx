import React, { useEffect, useRef } from 'react';
import { useGame } from '../context/GameContext';
import { publish } from '../api/websocketService';
import type { InviteToTeamRequest } from '../types/dto';

const PlayerContextMenu: React.FC = () => {
    const { gameState, dispatch } = useGame();
    const { contextMenu, sessionId } = gameState;
    const menuRef = useRef<HTMLDivElement>(null);

    // Эффект для закрытия меню при клике вне его области
    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
                dispatch({ type: 'CLOSE_CONTEXT_MENU' });
            }
        };

        if (contextMenu.isOpen) {
            document.addEventListener('mousedown', handleClickOutside);
        }

        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, [contextMenu.isOpen, dispatch]);

    if (!contextMenu.isOpen || !contextMenu.targetPlayer) {
        return null;
    }

    const handleInvite = () => {
        if (!contextMenu.targetPlayer) return;
        
        console.log(`Sending team invite to player ${contextMenu.targetPlayer.name}`);
        const payload: InviteToTeamRequest = { targetPlayerId: contextMenu.targetPlayer.id };
        publish(`/app/session/${sessionId}/team/invite`, payload);

        // Показываем уведомление об отправке
        dispatch({ 
            type: 'SHOW_NOTIFICATION', 
            payload: { message: `Invite sent to ${contextMenu.targetPlayer.name}!`, type: 'info' } 
        });

        dispatch({ type: 'CLOSE_CONTEXT_MENU' });
    };

    const handleClose = () => {
        dispatch({ type: 'CLOSE_CONTEXT_MENU' });
    };

    return (
        <div
            ref={menuRef}
            style={{
                position: 'fixed', // 'fixed' чтобы позиционироваться относительно окна браузера
                top: `${contextMenu.y}px`,
                left: `${contextMenu.x}px`,
                zIndex: 200,
                backgroundColor: 'rgba(40, 40, 40, 0.95)',
                border: '1px solid rgba(255, 255, 255, 0.2)',
                borderRadius: '6px',
                boxShadow: '0 5px 15px rgba(0,0,0,0.5)',
                color: 'white',
                fontFamily: 'system-ui, sans-serif',
                minWidth: '180px',
            }}
        >
            <div style={{ padding: '8px', borderBottom: '1px solid rgba(255,255,255,0.1)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ fontWeight: 'bold' }}>{contextMenu.targetPlayer.name}</span>
                <button onClick={handleClose} style={{ background: 'none', border: 'none', color: 'white', fontSize: '18px', cursor: 'pointer', padding: '0 5px' }}>
                    &times;
                </button>
            </div>
            <ul style={{ listStyle: 'none', margin: 0, padding: '5px' }}>
                <li
                    onClick={handleInvite}
                    style={{
                        padding: '8px 12px',
                        cursor: 'pointer',
                        borderRadius: '4px',
                    }}
                    onMouseEnter={(e) => e.currentTarget.style.backgroundColor = 'rgba(255,255,255,0.1)'}
                    onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                >
                    Invite to Team
                </li>
                {/* Здесь можно будет добавить другие пункты меню в будущем */}
            </ul>
        </div>
    );
};

export default PlayerContextMenu;