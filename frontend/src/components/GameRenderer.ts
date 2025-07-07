import { Application, Container, Graphics } from 'pixi.js';
import type { GameSessionState, PlayerAction, EntityClientState } from '../types/dto';
import { publish } from '../api/websocketService';

const TILE_SIZE = 32;

export class GameRenderer {
    private app: Application | null = null;
    private canvasContainer: HTMLDivElement;
    private gameState: GameSessionState | null = null;
    private hoveredEntityId: string | null = null;
    private damagedEntityId: string | null = null;

    private tileContainer: Container | null = null;
    private entityContainer: Container | null = null;
    private entityGraphics: Map<string, Graphics> = new Map();

    constructor(container: HTMLDivElement) {
        this.canvasContainer = container;
    }

    public update(newState: GameSessionState, newDamagedEntityId: string | null) {
        this.gameState = newState;
        this.damagedEntityId = newDamagedEntityId;

        if (!this.app && newState.mapState?.width > 0) {
            this.init(newState);
        }

        if (this.app) {
            this.draw();
        }
    }
    
    private init(initialState: GameSessionState) {
        if (this.app || !initialState.mapState) return;
        
        const { width, height } = initialState.mapState;
        
        const app = new Application();
        
        app.init({
            width: width * TILE_SIZE,
            height: height * TILE_SIZE,
            backgroundColor: 0x1d2327,
            antialias: true,
        }).then(() => {
            if (this.canvasContainer) {
                this.app = app;
                
                this.tileContainer = new Container();
                this.entityContainer = new Container();
                this.app.stage.addChild(this.tileContainer, this.entityContainer);

                this.canvasContainer.innerHTML = '';
                this.canvasContainer.appendChild(this.app.canvas);

                this.setupEventHandlers();
                this.draw();
            }
        });
    }

    private setupEventHandlers() {
        const app = this.app;
        if (!app) return;

        app.stage.interactive = true;
        
        app.stage.on('pointerdown', (event) => {
            if (!this.gameState?.sessionId) return;
            
            const pos = event.data.getLocalPosition(app.stage);
            const tileX = Math.floor(pos.x / TILE_SIZE);
            const tileY = Math.floor(pos.y / TILE_SIZE);
            
            const targetEntity = this.gameState.entities.find((e) => e.position.x === tileX && e.position.y === tileY);
            
            let action: PlayerAction;
            if (targetEntity && targetEntity.id !== this.gameState.yourPlayerId && !targetEntity.isDead) {
                action = { actionType: 'ATTACK', targetId: targetEntity.id };
            } else {
                action = { actionType: 'MOVE', targetPoint: { x: tileX, y: tileY } };
            }
            publish(`/app/session/${this.gameState.sessionId}/action`, action);
        });

        app.stage.on('pointermove', (event) => {
            if (!this.app || !this.gameState) return;
            const pos = event.data.getLocalPosition(app.stage);
            const tileX = Math.floor(pos.x / TILE_SIZE);
            const tileY = Math.floor(pos.y / TILE_SIZE);
            
            const targetEntity = this.gameState.entities.find((e) => e.position.x === tileX && e.position.y === tileY);
            const newHoverId = targetEntity && targetEntity.id !== this.gameState.yourPlayerId && !targetEntity.isDead ? targetEntity.id : null;
            
            this.setHoveredEntity(newHoverId);
        });

        app.stage.on('pointerout', () => {
            this.setHoveredEntity(null);
        });
    }
    
    private setHoveredEntity(entityId: string | null) {
        this.hoveredEntityId = entityId;
        if (this.canvasContainer) {
            this.canvasContainer.style.cursor = entityId ? 'crosshair' : 'default';
        }
        this.draw();
    }

    private draw() {
        const app = this.app;
        const state = this.gameState;
        const tileContainer = this.tileContainer;
        const entityContainer = this.entityContainer;

        if (!app || !state?.mapState?.width || !tileContainer || !entityContainer) {
            return;
        }

        if (tileContainer.children.length === 0) {
            state.mapState.tiles.forEach((tileType, index) => {
                const x = (index % state.mapState.width) * TILE_SIZE;
                const y = Math.floor(index / state.mapState.width) * TILE_SIZE;
                const tileGraphics = new Graphics();
                const color = tileType === 'WALL' ? 0x333333 : 0xAAAAAA;
                tileGraphics.beginFill(color);
                tileGraphics.lineStyle(1, 0x000000, 0.2); 
                tileGraphics.drawRect(x, y, TILE_SIZE, TILE_SIZE);
                tileGraphics.endFill();
                tileContainer.addChild(tileGraphics);
            });
        }
    
        const seenEntityIds = new Set<string>();
        state.entities.forEach((entity: EntityClientState) => {
            seenEntityIds.add(entity.id);
            let graphics = this.entityGraphics.get(entity.id);

            if (!graphics) {
                graphics = new Graphics();
                this.entityGraphics.set(entity.id, graphics);
                entityContainer.addChild(graphics);
            }
            graphics.clear();
            
            const isSelf = entity.id === state.yourPlayerId;
            const isHoveredEnemy = entity.id === this.hoveredEntityId;
            const isDamaged = entity.id === this.damagedEntityId;
    
            if (entity.isDead) {
                graphics.beginFill(0x555555, 0.7);
                graphics.lineStyle(1, 0x222222, 0.7);
            } else {
                const color = isDamaged ? 0xFFFFFF : (entity.type === 'PLAYER' ? 0x00FF00 : 0xFF0000);
                graphics.beginFill(color);
                if (isSelf) graphics.lineStyle(2, 0xFFFFFF, 1);
                else if (isHoveredEnemy) graphics.lineStyle(2, 0xFFFF00, 1);
            }

            graphics.drawCircle(0, 0, TILE_SIZE / 2 - 2);
            graphics.endFill();

            graphics.position.set(
                entity.position.x * TILE_SIZE + TILE_SIZE / 2,
                entity.position.y * TILE_SIZE + TILE_SIZE / 2
            );
        });

        this.entityGraphics.forEach((graphics, entityId) => {
            if (!seenEntityIds.has(entityId)) {
                entityContainer.removeChild(graphics);
                graphics.destroy();
                this.entityGraphics.delete(entityId);
            }
        });
    }

    public destroy() {
        if (this.app) {
            this.app.destroy(true, { children: true });
            this.app = null;
        }
    }
}