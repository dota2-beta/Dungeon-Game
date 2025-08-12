import React, { useEffect } from 'react';
import { useGame } from '../context/GameContext';

const NotificationUI: React.FC = () => {
    const { gameState, dispatch } = useGame();
    const { notification } = gameState;

    useEffect(() => {
        if (notification) {
            const timer = setTimeout(() => {
                dispatch({ type: 'HIDE_NOTIFICATION' });
            }, 4000); 

            return () => clearTimeout(timer);
        }
    }, [notification, dispatch]); 

    if (!notification) {
        return null;
    }

    const getBackgroundColor = () => {
        switch (notification.type) {
            case 'success': return '#2ecc71'; 
            case 'error': return '#e74c3c';  
            case 'info':
            default: return '#3498db'; 
        }
    };

    const notificationStyle: React.CSSProperties = {
        position: 'absolute',
        top: '100px', 
        left: '50%',
        transform: 'translateX(-50%)',
        zIndex: 200, 
        padding: '12px 24px',
        backgroundColor: getBackgroundColor(),
        color: 'white',
        borderRadius: '8px',
        fontSize: '18px',
        fontWeight: 'bold',
        boxShadow: '0 5px 15px rgba(0,0,0,0.3)',
        animation: 'fadeInOut 4s ease-in-out',
    };

    return (
        <>
            <style>
                {`
                    @keyframes fadeInOut {
                        0% { opacity: 0; transform: translate(-50%, -20px); }
                        15% { opacity: 1; transform: translate(-50%, 0); }
                        85% { opacity: 1; transform: translate(-50%, 0); }
                        100% { opacity: 0; transform: translate(-50%, 20px); }
                    }
                `}
            </style>
            <div style={notificationStyle}>
                {notification.message}
            </div>
        </>
    );
};

export default NotificationUI;