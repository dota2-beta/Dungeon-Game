import React, { useEffect, useRef } from 'react';
import { useGame } from '../context/GameContext';
import { GameRenderer } from './GameRenderer';

const GameCanvas: React.FC = () => {
    const canvasRef = useRef<HTMLDivElement>(null);
    const rendererRef = useRef<GameRenderer | null>(null);
    const { gameState, dispatch } = useGame();
    
    useEffect(() => {
        if (canvasRef.current && !rendererRef.current) {
            rendererRef.current = new GameRenderer(canvasRef.current, dispatch); 
        }
        
        if (rendererRef.current) {
            rendererRef.current.update(gameState);
        }
    }, [gameState, dispatch]);

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

                setTimeout(() => dispatch({ type: 'CLEAR_COMBAT_ANIMATIONS' }), 0);
            }
        }
    }, [gameState.lastAttack, gameState.lastDamage, dispatch]);

    useEffect(() => {
        const moveInfo = gameState.lastMove;

        if (moveInfo && rendererRef.current) {
            rendererRef.current.animateMovement(moveInfo.payload);
            
            setTimeout(() => dispatch({ type: 'CLEAR_MOVE_ANIMATION' }), 0);
        }
    }, [gameState.lastMove, dispatch]);


    useEffect(() => {
        return () => {
            rendererRef.current?.destroy();
            rendererRef.current = null;
        }
    }, []);

    useEffect(() => {
        const castInfo = gameState.lastAbilityCast;

        if (castInfo && rendererRef.current) {
            rendererRef.current.playAbilityAnimation(castInfo.payload);
            
            setTimeout(() => dispatch({ type: 'CLEAR_ABILITY_ANIMATION' }), 0);
        }
    }, [gameState.lastAbilityCast, dispatch]);

    return <div ref={canvasRef} style={{ cursor: 'default' }} />;
};

export default GameCanvas;