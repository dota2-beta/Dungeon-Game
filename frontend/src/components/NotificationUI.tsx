import React, { useEffect } from 'react';
import { useGame } from '../context/GameContext';

const NotificationUI: React.FC = () => {
    const { gameState, dispatch } = useGame();
    const { notification } = gameState;

    useEffect(() => {
        if (notification) {
            const timer = setTimeout(() => {
                dispatch({ type: 'HIDE_NOTIFICATION' });
            }, 5000);

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
        backgroundColor: getBackgroundColor(),
        color: 'white',
        borderRadius: '8px',
        fontSize: '18px',
        fontWeight: 'bold',
        boxShadow: '0 5px 15px rgba(0,0,0,0.3)',
        animation: 'fadeInOut 15s ease-in-out',
        width: 'auto',      
        minWidth: '350px',
        maxWidth: '80%',   
        textAlign: 'center',
        wordBreak: 'break-word',
        padding: '16px 24px', 
        lineHeight: '1.4',
    };

    return (
        <>
            <style>
                {`
                    @keyframes fadeInOut {
                        0% { opacity: 0; transform: translate(-50%, -20px); }
                        5% { opacity: 1; transform: translate(-50%, 0); } /* Быстрое появление */
                        95% { opacity: 1; transform: translate(-50%, 0); } /* Долгое отображение */
                        100% { opacity: 0; transform: translate(-50%, 20px); } /* Плавное исчезновение */
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