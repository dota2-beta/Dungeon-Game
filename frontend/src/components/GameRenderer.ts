import { Application, Container, Graphics, Text, TextStyle } from 'pixi.js';
import type { GameSessionState, PlayerAction, EntityState, EntityStatsUpdatedEvent, Point } from '../types/dto';
import { publish } from '../api/websocketService';
import { gsap } from 'gsap';

const TILE_SIZE = 32;

interface DamageTextAnimation {
    text: Text;
    startTime: number;
    duration: number;
}

export class GameRenderer {
    private app: Application | null = null;
    private canvasContainer: HTMLDivElement;

    private tileContainer: Container | null = null;
    private entityContainer: Container | null = null;
    private uiContainer: Container | null = null;

    private gameState: GameSessionState | null = null;
    private entityGraphics: Map<string, Graphics> = new Map();
    private hoveredEntityId: string | null = null;
    private activeDamageTexts: DamageTextAnimation[] = [];
    private damagedEntityInfo: { id: string; clearTime: number } | null = null;

    constructor(container: HTMLDivElement) {
        this.canvasContainer = container;
    }

    public async init(initialState: GameSessionState) {
        if (this.app || !initialState.mapState) return;
        
        const { width, height } = initialState.mapState;
        
        this.app = new Application();
        
        await this.app.init({
            width: width * TILE_SIZE,
            height: height * TILE_SIZE,
            backgroundColor: 0x1d2327,
            antialias: true,
        });

        if (this.canvasContainer) {
            this.tileContainer = new Container();
            this.entityContainer = new Container();
            this.uiContainer = new Container();
            this.app.stage.addChild(this.tileContainer, this.entityContainer, this.uiContainer);

            this.canvasContainer.innerHTML = '';
            this.canvasContainer.appendChild(this.app.canvas);

            this.setupEventHandlers();
            this.app.ticker.add(this.updateAnimations.bind(this));
            this.draw();
        }
    }

    public update(newState: GameSessionState) {
        this.gameState = newState;

        if (!this.app && newState.mapState?.width > 0) {
            this.init(newState);
        } else if (this.app) {
            this.draw();
        }
    }
    
    private setupEventHandlers() {
        if (!this.app) return;

        this.app.stage.interactive = true;
        
        this.app.stage.on('pointerdown', (event) => {
            if (!this.gameState?.sessionId || !this.app) return;
            
            const pos = event.data.getLocalPosition(this.app.stage);
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

        this.app.stage.on('pointermove', (event) => {
            if (!this.app || !this.gameState) return;
            const pos = event.data.getLocalPosition(this.app.stage);
            const tileX = Math.floor(pos.x / TILE_SIZE);
            const tileY = Math.floor(pos.y / TILE_SIZE);
            
            const targetEntity = this.gameState.entities.find((e) => e.position.x === tileX && e.position.y === tileY);
            const newHoverId = targetEntity && targetEntity.id !== this.gameState.yourPlayerId && !targetEntity.isDead ? targetEntity.id : null;
            
            if (newHoverId !== this.hoveredEntityId) {
                this.hoveredEntityId = newHoverId;
                this.canvasContainer.style.cursor = newHoverId ? 'crosshair' : 'default';
                this.draw();
            }
        });

        this.app.stage.on('pointerout', () => {
             if (this.hoveredEntityId) {
                this.hoveredEntityId = null;
                this.canvasContainer.style.cursor = 'default';
                this.draw();
            }
        });
    }

    private draw() {
        if (!this.app || !this.gameState || !this.tileContainer || !this.entityContainer) {
            return;
        }
        const state = this.gameState;

        if (this.tileContainer.children.length === 0 && state.mapState.width > 0) {
            state.mapState.tiles.forEach((tileType, index) => {
                const x = (index % state.mapState.width) * TILE_SIZE;
                const y = Math.floor(index / state.mapState.width) * TILE_SIZE;
                const tileGraphics = new Graphics();
                const color = tileType === 'WALL' ? 0x333333 : 0xAAAAAA;
                tileGraphics.beginFill(color).lineStyle(1, 0x000000, 0.2).drawRect(x, y, TILE_SIZE, TILE_SIZE).endFill();
                this.tileContainer!.addChild(tileGraphics);
            });
        }
    
        const seenEntityIds = new Set<string>();
        state.entities.forEach((entity: EntityState) => {
            seenEntityIds.add(entity.id);
            let graphics = this.entityGraphics.get(entity.id);

            if (!graphics) {
                graphics = new Graphics();
                this.entityGraphics.set(entity.id, graphics);
                this.entityContainer!.addChild(graphics);
            }
            graphics.clear();
            
            const isSelf = entity.id === state.yourPlayerId;
            const isHoveredEnemy = this.hoveredEntityId === entity.id;
            const isDamaged = this.damagedEntityInfo?.id === entity.id;

            if (entity.isDead) {
                graphics.beginFill(0x333333, 0.6);
                graphics.lineStyle(1, 0x000000, 0.6);
                graphics.tint = 0xFFFFFF;
            } else {
                const baseColor = entity.type === 'PLAYER' ? 0x00FF00 : 0xFF0000;
                const finalColor = isDamaged ? 0xFFFFFF : baseColor;
                graphics.beginFill(finalColor, 1.0);
                
                if (isDamaged) {
                    graphics.tint = 0xFF0000;
                } else {
                    graphics.tint = 0xFFFFFF;
                }

                if (isSelf) {
                    graphics.lineStyle(2, 0xFFFFFF, 1);
                } else if (isHoveredEnemy) {
                    graphics.lineStyle(2, 0xFFFF00, 1);
                } else {
                    graphics.lineStyle(1, 0x000000, 0.5);
                }
            }

            graphics.drawCircle(0, 0, TILE_SIZE / 2 - 2);
            graphics.endFill();

            if (!gsap.isTweening(graphics.position)) {
                graphics.position.set(
                    entity.position.x * TILE_SIZE + TILE_SIZE / 2,
                    entity.position.y * TILE_SIZE + TILE_SIZE / 2
                );
            }
        });

        this.entityGraphics.forEach((graphics, entityId) => {
            if (!seenEntityIds.has(entityId)) {
                this.entityContainer!.removeChild(graphics);
                graphics.destroy();
                this.entityGraphics.delete(entityId);
            }
        });
    }

    public playAttackAnimation(attackerId: string, targetId: string) {
        const attackerGfx = this.entityGraphics.get(attackerId);
        const targetGfx = this.entityGraphics.get(targetId);

        if (!attackerGfx || !targetGfx) return;
        
        gsap.to(attackerGfx.position, {
            x: targetGfx.x,
            y: targetGfx.y,
            duration: 0.15,
            ease: 'power1.inOut',
            yoyo: true,
            repeat: 1,
        });
    }

    public showDamageNumber(payload: EntityStatsUpdatedEvent) {
        const targetGfx = this.entityGraphics.get(payload.targetEntityId);
        if (!targetGfx || !this.uiContainer) return;

        const style = new TextStyle({
            fontFamily: 'Arial, sans-serif',
            fontSize: 22,
            fontWeight: 'bold',
            fill: '#ff4444',
            stroke: { color: '#000000', width: 4, join: 'round' },
        });

        const damageText = new Text({ text: `-${payload.damageToHp}`, style });
        damageText.anchor.set(0.5);
        damageText.position.set(targetGfx.x, targetGfx.y - TILE_SIZE / 2);

        this.uiContainer.addChild(damageText);
        
        this.activeDamageTexts.push({
            text: damageText,
            startTime: Date.now(),
            duration: 1200,
        });
    }
    
    public flashEntity(entityId: string, duration: number = 300) {
        this.damagedEntityInfo = {
            id: entityId,
            clearTime: Date.now() + duration,
        };
        this.draw();
    }

    private updateAnimations(ticker: any) {
        const now = Date.now();
        const delta = ticker.deltaMS;

        if (this.damagedEntityInfo && now > this.damagedEntityInfo.clearTime) {
            this.damagedEntityInfo = null;
            this.draw();
        }

        this.activeDamageTexts = this.activeDamageTexts.filter(anim => {
            const elapsedTime = now - anim.startTime;
            if (elapsedTime >= anim.duration) {
                this.uiContainer?.removeChild(anim.text);
                anim.text.destroy();
                return false;
            }

            anim.text.y -= 0.6 * (delta / 16.67);
            anim.text.alpha = 1 - (elapsedTime / anim.duration);
            return true;
        });
    }

    public destroy() {
        if (this.app) {
            this.app.ticker.stop();
            gsap.globalTimeline.clear();
            this.app.destroy(true, { children: true, texture: true });
            this.app = null;
            this.canvasContainer.innerHTML = '';
            console.log("GameRenderer destroyed.");
        }
    }
}