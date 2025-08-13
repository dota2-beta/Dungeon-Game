import React, { useRef, useEffect } from 'react';
import { useGame } from '../context/GameContext';
import { GameRenderer } from './GameRenderer';

const GameCanvas: React.FC = () => {
    const canvasRef = useRef<HTMLDivElement>(null);
    const rendererRef = useRef<GameRenderer | null>(null);
    const { gameState, dispatch } = useGame();
    
    useEffect(() => {
        if (canvasRef.current && !rendererRef.current) {
            console.log("GameCanvas: Initializing GameRenderer...");
            rendererRef.current = new GameRenderer(canvasRef.current, dispatch); 
        }
        
        if (rendererRef.current) {
            console.log('%c[DEBUG] GameCanvas: Calling renderer.update() with gameState:', 'color: blue; font-weight: bold;', {
                abilitiesInState: gameState.abilities,
                selectedAbility: gameState.selectedAbility,
            });
            // --- END DEBUG ---
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
                    console.log("GameCanvas: SYNC! Both attack and damage events received. Running animations.");
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
            console.log("GameCanvas: Detected move event, running animation.");
            
            rendererRef.current.animateMovement(moveInfo.payload);
            
            setTimeout(() => dispatch({ type: 'CLEAR_MOVE_ANIMATION' }), 0);
        }
    }, [gameState.lastMove, dispatch]);


    useEffect(() => {
        return () => {
            console.log("GameCanvas: Component unmounting, destroying renderer.");
            rendererRef.current?.destroy();
            rendererRef.current = null;
        }
    }, []);

    useEffect(() => {
        const castInfo = gameState.lastAbilityCast;

        if (castInfo && rendererRef.current) {
            console.log("GameCanvas: Detected ability cast event, running animation.");
            
            rendererRef.current.playAbilityAnimation(castInfo.payload);
            
            setTimeout(() => dispatch({ type: 'CLEAR_ABILITY_ANIMATION' }), 0);
        }
    }, [gameState.lastAbilityCast, dispatch]);

    return <div ref={canvasRef} style={{ cursor: 'default' }} />;
};

export default GameCanvas;