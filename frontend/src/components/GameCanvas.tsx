import React, { useRef, useEffect, useState } from 'react';
import { useGame } from '../context/GameContext';
import { GameRenderer } from './GameRenderer';
import type { EntityClientState } from '../types/dto';

const GameCanvas: React.FC = () => {
    const canvasRef = useRef<HTMLDivElement>(null);
    const rendererRef = useRef<GameRenderer | null>(null);
    const { gameState } = useGame();
    
    // Состояние для анимации урона
    const [damagedEntityId, setDamagedEntityId] = useState<string | null>(null);
    const prevEntitiesRef = useRef<EntityClientState[]>([]);

    // Основной useEffect для создания и обновления рендерера
    useEffect(() => {
        // Создаем рендерер один раз при монтировании
        if (canvasRef.current && !rendererRef.current) {
            rendererRef.current = new GameRenderer(canvasRef.current);
        }
        
        // Вызываем update при каждом изменении gameState
        if (rendererRef.current) {
            rendererRef.current.update(gameState, damagedEntityId);
        }

        // Функция очистки при размонтировании
        return () => {
            if (rendererRef.current) {
                // Уничтожаем рендерер, только если компонент действительно размонтируется
            }
        };
    }, [gameState, damagedEntityId])

    // Отдельный useEffect для отслеживания атак и запуска анимации
    useEffect(() => {
        gameState.entities.forEach(currentEntity => {
            const prevEntity = prevEntitiesRef.current.find(p => p.id === currentEntity.id);
            if (prevEntity && currentEntity.currentHp < prevEntity.currentHp) {
                setDamagedEntityId(currentEntity.id);
                setTimeout(() => setDamagedEntityId(null), 200);
            }
        });
        prevEntitiesRef.current = gameState.entities;
    }, [gameState.entities]);


    // При размонтировании компонента GameCanvas полностью уничтожаем Pixi
    useEffect(() => {
        return () => {
            rendererRef.current?.destroy();
            rendererRef.current = null;
        }
    }, []);

    return <div ref={canvasRef} />;
};

export default GameCanvas;