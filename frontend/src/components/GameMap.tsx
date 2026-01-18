import React, { useState, useRef, useEffect, useMemo } from 'react';
import { useGameState } from '../store/GameContext';
import { hexToPixel, getTileColor, HEX_SIZE } from '../utils/hexMath';
import { type EntityStateDto, type TileDto } from '../types/game';
import { gameSocket } from '../api/GameSocket';

const GameMap: React.FC = () => {
    const { state, dispatch } = useGameState();
    const { map, entities, userId, sessionId } = state;
    
    const latestEntitiesRef = useRef(entities);
    
    latestEntitiesRef.current = entities;

    const containerRef = useRef<HTMLDivElement>(null);
    const [view, setView] = useState({ x: 0, y: 0, scale: 1 });
    const [isDragging, setIsDragging] = useState(false);
    
    const dragStart = useRef({ x: 0, y: 0 }); 
    const lastMouse = useRef({ x: 0, y: 0 });

    const getMyPlayer = (): EntityStateDto | undefined => {
        return Object.values(entities).find(e => e.userId === userId); 
    };
    const myPlayerRender = getMyPlayer();

    // Блокировка скролла
    useEffect(() => {
        const container = containerRef.current;
        if (!container) return;
        const onWheel = (e: WheelEvent) => {
            e.preventDefault();
            const newScale = Math.max(0.5, Math.min(3, view.scale - e.deltaY * 0.001));
            setView(prev => ({ ...prev, scale: newScale }));
        };
        container.addEventListener('wheel', onWheel, { passive: false });
        return () => container.removeEventListener('wheel', onWheel);
    }, [view.scale]);

    // --- Mouse Handlers ---
    const handleMouseDown = (e: React.MouseEvent) => {
        setIsDragging(true);
        lastMouse.current = { x: e.clientX, y: e.clientY };
        dragStart.current = { x: e.clientX, y: e.clientY };
    };

    const handleMouseMove = (e: React.MouseEvent) => {
        if (!isDragging) return;
        const dx = e.clientX - lastMouse.current.x;
        const dy = e.clientY - lastMouse.current.y;
        lastMouse.current = { x: e.clientX, y: e.clientY };
        setView(prev => ({ ...prev, x: prev.x + dx, y: prev.y + dy }));
    };

    const handleMouseUp = () => setIsDragging(false);

    // --- Logic ---

    const handleHexClick = (tile: TileDto) => {
        const dist = Math.hypot(
            lastMouse.current.x - dragStart.current.x,
            lastMouse.current.y - dragStart.current.y
        );
        if (dist > 5) return;

        // 🔥 ВАЖНО: Берем данные из REFA, а не из замыкания
        const currentEntities = latestEntitiesRef.current;
        
        // Ищем игрока заново в актуальных данных
        const currentPlayer = Object.values(currentEntities).find(e => e.userId === userId);

        if (!currentPlayer || !sessionId) return;

        // Ищем цель в актуальных данных
        const targetEntity = Object.values(currentEntities).find(
            e => e.position.q === tile.coordinate.q && e.position.r === tile.coordinate.r
        );

        if (targetEntity) {
            if (targetEntity.id !== currentPlayer.id) {
                console.log(`⚔️ ATTACK: ${currentPlayer.name} -> ${targetEntity.name}`);
                gameSocket.attack(sessionId, currentPlayer.id, targetEntity.id);
            } else {
                console.log("Clicked self (Info)");
            }
        } else {
            console.log(`👣 MOVE: [${tile.coordinate.q}, ${tile.coordinate.r}]`);
            gameSocket.move(sessionId, tile.coordinate);
        }
    };

    // --- Rendering ---

    const handleMouseEnter = (entity: EntityStateDto) => {
        dispatch({ type: 'SET_HOVER', payload: entity.id });
    };
    const handleMouseLeave = () => {
        dispatch({ type: 'SET_HOVER', payload: null });
    };

    // Тайлы мемоизированы и зависят ТОЛЬКО от карты.
    // Благодаря latestEntitiesRef внутри handleHexClick, клик будет работать с новыми данными.
    const renderedTiles = useMemo(() => {
        if (!map) return null;
        return map.tiles.map((tile: TileDto, index: number) => {
            const { x, y } = hexToPixel(tile.coordinate);
            return (
                <g key={`tile-${index}`} transform={`translate(${x}, ${y})`}
                    onClick={(e) => {
                        e.stopPropagation();
                        handleHexClick(tile);
                    }}
                    style={{ cursor: 'pointer' }}
                >
                    <polygon points={calculateHexPoints(HEX_SIZE)}
                        fill={getTileColor(tile.type)}
                        stroke="#444" strokeWidth="1"
                    />
                    {/* Координаты можно скрыть или оставить для отладки */}
                    {/* <text x="0" y="4" fontSize="8" ... >...</text> */}
                </g>
            );
        });
    }, [map]); 

    // Сущности рисуем как обычно (они не мемоизированы так жестко)
    const renderedEntities = Object.values(entities).map((entity) => {
        const { x, y } = hexToPixel(entity.position);
        const isMe = entity.id === myPlayerRender?.id; // Используем переменную из рендера
        
        let color = '#F44336'; 
        if (entity.currentHp <= 0) color = '#9E9E9E';
        else if (entity.type === 'PLAYER') {
            color = isMe ? '#4CAF50' : '#2196F3';
        }

        return (
            <g key={entity.id} transform={`translate(${x}, ${y})`} 
                style={{
                    pointerEvents: 'auto', 
                    transition: 'transform 0.2s ease-out',
                    opacity: entity.currentHp <= 0 ? 0.6 : 1,
                    zIndex: entity.currentHp <= 0 ? 0 : 10
                }}
                onMouseEnter={() => handleMouseEnter(entity)}
                onMouseLeave={handleMouseLeave}
                onClick={(e) => {
                    e.stopPropagation();
                    const tile = map?.tiles.find(t => t.coordinate.q === entity.position.q && t.coordinate.r === entity.position.r);
                    if (tile) handleHexClick(tile);
                }}
            >
                 <circle
                    r={HEX_SIZE * 0.7}
                    fill={color}
                    stroke="white"
                    strokeWidth={isMe ? 3 : 1}
                    style={{ cursor: 'crosshair' }}
                />
                {entity.currentHp > 0 && (
                    <>
                        <rect x="-15" y="-20" width="30" height="4" fill="red" />
                        <rect x="-15" y="-20" width={Math.max(0, 30 * (entity.currentHp / entity.maxHp))} height="4" fill="#0f0" />
                    </>
                )}
            </g>
        );
    });

    if (!map) return <div>Loading Map...</div>;

    return (
        <div ref={containerRef}
            style={{ overflow: 'hidden', width: '100%', height: '100%', background: '#222', position: 'relative', cursor: isDragging ? 'grabbing' : 'grab' }}
            onMouseDown={handleMouseDown} onMouseMove={handleMouseMove} onMouseUp={handleMouseUp} onMouseLeave={handleMouseUp}
        >
            <svg width="100%" height="100%">
                <g transform={`translate(${view.x + 400}, ${view.y + 300}) scale(${view.scale})`}>
                    {renderedTiles}
                    {renderedEntities}
                </g>
            </svg>
        </div>
    );
};

function calculateHexPoints(size: number): string {
    const angles = [30, 90, 150, 210, 270, 330];
    return angles.map(angle => {
        const rad = Math.PI / 180 * angle;
        return `${size * Math.cos(rad)},${size * Math.sin(rad)}`;
    }).join(' ');
}

export default GameMap;