import React, { useRef, useEffect } from 'react';
import { useGame } from '../context/GameContext';
import { GameRenderer } from './GameRenderer';

const GameCanvas: React.FC = () => {
    const canvasRef = useRef<HTMLDivElement>(null);
    const rendererRef = useRef<GameRenderer | null>(null);
    const { gameState, dispatch } = useGame();
    
    useEffect(() => {
        if (canvasRef.current && !rendererRef.current) {
            rendererRef.current = new GameRenderer(canvasRef.current);
        }
        
        if (rendererRef.current) {
            rendererRef.current.update(gameState); 
        }
    }, [gameState]);

    useEffect(() => {
        const attackInfo = gameState.lastAttack;
        const damageInfo = gameState.lastDamage;

        if (attackInfo && damageInfo && attackInfo.payload.targetEntityId === damageInfo.payload.targetEntityId) {
            if (Math.abs(attackInfo.timestamp - damageInfo.timestamp) < 500) {
                const renderer = rendererRef.current;
                if (renderer) {
                    renderer.playAttackAnimation(attackInfo.payload.attackerEntityId, attackInfo.payload.targetEntityId);
                    renderer.showDamageNumber(damageInfo.payload);
                    renderer.flashEntity(damageInfo.payload.targetEntityId, 400);
                }
                dispatch({ type: 'CLEAR_COMBAT_ANIMATIONS' });
            }
        }
    }, [gameState.lastAttack, gameState.lastDamage, dispatch]);

    useEffect(() => {
        return () => {
            rendererRef.current?.destroy();
            rendererRef.current = null;
        }
    }, []);

    return <div ref={canvasRef} style={{ cursor: 'default' }} />;
};

export default GameCanvas;