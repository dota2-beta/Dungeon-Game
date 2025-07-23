import { Application, Container, Graphics, Text, TextStyle } from 'pixi.js';
import type { GameSessionStateDto, PlayerAction, EntityStateDto, EntityStatsUpdatedEvent, Hex, EntityMovedEvent, TileDto } from '../types/dto';
import { publish } from '../api/websocketService';
import { gsap } from 'gsap';
import { Grid, AStarFinder } from 'pathfinding';
import { Pathfinder } from '../game/Pathfinder';

const HEX_SIZE = 24;

const hexToPixel = (hex: Hex): { x: number; y: number } => {
    const x = HEX_SIZE * (Math.sqrt(3) * hex.q + (Math.sqrt(3) / 2) * hex.r);
    const y = HEX_SIZE * (3/2 * hex.r);
    return { x, y };
};

const pixelToHex = (x: number, y: number): Hex => {
    const q = ((Math.sqrt(3)/3) * x - (1/3) * y) / HEX_SIZE;
    const r = ((2/3) * y) / HEX_SIZE;
    return hexRound({ q, r });
};

const hexRound = (frac: { q: number; r: number }): Hex => {
    const s_frac = -frac.q - frac.r;
    let q = Math.round(frac.q);
    let r = Math.round(frac.r);
    let s = Math.round(s_frac);
    const q_diff = Math.abs(q - frac.q);
    const r_diff = Math.abs(r - frac.r);
    const s_diff = Math.abs(s - s_frac);

    if (q_diff > r_diff && q_diff > s_diff) q = -r - s;
    else if (r_diff > s_diff) r = -q - s;
    return { q, r };
};

const hexSubtract = (a: Hex, b: Hex): Hex => ({ q: a.q - b.q, r: a.r - b.r });

const hexDistance = (a: Hex, b: Hex): number => {
    const vec = hexSubtract(a, b);
    const s = -vec.q - vec.r;
    return (Math.abs(vec.q) + Math.abs(vec.r) + Math.abs(s)) / 2;
};

const HEX_DIRECTIONS = [
    { q: 1, r: 0 }, { q: 1, r: -1 }, { q: 0, r: -1 },
    { q: -1, r: 0 }, { q: -1, r: 1 }, { q: 0, r: 1 }
];
const hexAdd = (a: Hex, b: Hex): Hex => ({ q: a.q + b.q, r: a.r + b.r });
const getNeighbor = (hex: Hex, direction: number): Hex => hexAdd(hex, HEX_DIRECTIONS[direction]);

export class GameRenderer {
    private app: Application | null = null;
    private canvasContainer: HTMLDivElement;
    private worldContainer: Container | null = null;
    private tileContainer: Container | null = null;
    private entityContainer: Container | null = null;
    private uiContainer: Container | null = null;

    private gameState: GameSessionStateDto | null = null;
    private entityGraphics: Map<string, Graphics> = new Map();
    private hoveredEntityId: string | null = null;
    private damagedEntityInfo: { id: string; clearTime: number } | null = null;

    private pathfinder: Pathfinder | null = null;
    private isMovingAlongPath = false;
    private movementQueue: Hex[] = [];
    private isExecutingMove = false;

    constructor(container: HTMLDivElement) {
        this.canvasContainer = container;
    }

    public async init(initialState: GameSessionStateDto) {
        if (this.app || !initialState.mapState) return;

        const canvasWidth = 1200;
        const canvasHeight = 800;

        this.app = new Application();
        await this.app.init({ width: canvasWidth, height: canvasHeight, backgroundColor: 0x1d2327, antialias: true });

        if (this.canvasContainer) {
            this.worldContainer = new Container();
            this.tileContainer = new Container();
            this.entityContainer = new Container();
            this.uiContainer = new Container();
            this.worldContainer.addChild(this.tileContainer, this.entityContainer, this.uiContainer);
            this.app.stage.addChild(this.worldContainer);
            this.worldContainer.position.set(canvasWidth / 2, canvasHeight / 2);

            this.canvasContainer.innerHTML = '';
            this.canvasContainer.appendChild(this.app.canvas);

            this.setupEventHandlers();
            this.app.ticker.add(this.updateAnimations.bind(this));
            
            this.gameState = initialState;
            
            console.log('%cRenderer: Building Pathfinder...', 'color: blue');
            this.pathfinder = new Pathfinder(initialState.mapState);
            
            console.log('%cRenderer: Performing initial draw...', 'color: blue');
            this.drawMap();
            this.drawEntities();

            const center: Hex = { q: 0, r: 0 };
            console.log("%c--- CLIENT-SIDE NEIGHBORS for (0,0) ---", "color: yellow");
            for (let i = 0; i < 6; i++) {
                const neighbor = getNeighbor(center, i);
                console.log(`%cDirection ${i}: q=${neighbor.q}, r=${neighbor.r}`, "color: yellow");
            }
            console.log("%c------------------------------------", "color: yellow");
            }
    }

    public update(newState: GameSessionStateDto) {
        if (!this.app) {
            if (newState.mapState?.tiles.length > 0) {
                this.init(newState);
            }
            return;
        }

        this.gameState = newState;
        this.drawEntities();
    }
    
    private setupEventHandlers() {
        if (!this.app || !this.worldContainer) return;
        this.worldContainer.interactive = true;
        
        this.worldContainer.on('pointerdown', (event) => {
            if (!this.gameState || this.movementQueue.length > 0) return; 

            const player = this.gameState.entities.find((e: EntityStateDto) => e.id === this.gameState!.yourPlayerId);
            if (!player) return;

            const pos = event.data.getLocalPosition(this.worldContainer!);
            const targetHex = pixelToHex(pos.x, pos.y);
            
            const targetEntity = this.gameState.entities.find((e: EntityStateDto) => e.position.q === targetHex.q && e.position.r === targetHex.r);

            if (targetEntity && targetEntity.id !== player.id && !targetEntity.dead) {
                publish(`/app/session/${this.gameState.sessionId}/action`, { actionType: 'ATTACK', targetId: targetEntity.id });
            } else {
                this.handleMoveRequest(player, targetHex);
            }
        });
        
        this.worldContainer.on('pointermove', (event) => {
            if (!this.app || !this.gameState) return;
            const pos = event.data.getLocalPosition(this.worldContainer!);
            const hoveredHex = pixelToHex(pos.x, pos.y);

            const targetEntity = this.gameState.entities.find((e: EntityStateDto) => e.position.q === hoveredHex.q && e.position.r === hoveredHex.r);
            const player = this.gameState.entities.find((e: EntityStateDto) => e.id === this.gameState!.yourPlayerId);
            
            let newHoverId: string | null = null;

            if (player && targetEntity && targetEntity.id !== player.id && !targetEntity.dead) {
                if (hexDistance(player.position, targetEntity.position) <= player.attackRange) {
                    newHoverId = targetEntity.id;
                }
            }
            
            if (newHoverId !== this.hoveredEntityId) {
                this.hoveredEntityId = newHoverId;
                this.canvasContainer.style.cursor = newHoverId ? 'crosshair' : 'default';
                this.drawEntities();
            }
        });
    }
    
    private handleMoveRequest(player: EntityStateDto, targetHex: Hex) {
        if (!this.pathfinder || !this.gameState) return; 
    
        const path = this.pathfinder.findPath(player.position, targetHex, this.gameState.entities);
        
        if (path.length > 1) {
            this.executeMovePath(path);
        } else {
            console.warn("No path found.");
        }
    }

    private executeNextMoveStep() {
        if (this.movementQueue.length === 0) {
            console.log("Path finished.");
            this.isExecutingMove = false;
            return;
        }

        const player = this.gameState!.entities.find((e: EntityStateDto) => e.id === this.gameState!.yourPlayerId)!;
        if (player.state === 'COMBAT' && player.currentAP < 1) {
            console.log("Out of AP, stopping path.");
            this.movementQueue = []; 
            this.isExecutingMove = false;
            return;
        }

        this.isExecutingMove = true;
        const nextStep = this.movementQueue[0];

        console.log(`Sending move step to: q=${nextStep.q}, r=${nextStep.r}`);
        publish(`/app/session/${this.gameState!.sessionId}/action`, {
            actionType: 'MOVE',
            targetHex: nextStep
        });
    }

    private async executeMovePath(path: Hex[]) {
        if (this.isMovingAlongPath) return;
        this.isMovingAlongPath = true;

        for (let i = 1; i < path.length; i++) {
            const player = this.gameState!.entities.find((e: EntityStateDto) => e.id === this.gameState!.yourPlayerId)!;
            if (player.state === 'COMBAT' && player.currentAP < 1) {
                console.log("Out of AP, stopping movement.");
                break;
            }

            const nextStep = path[i];
            publish(`/app/session/${this.gameState!.sessionId}/action`, {
                actionType: 'MOVE',
                targetHex: nextStep
            });
            
            await new Promise(resolve => setTimeout(resolve, 310));
        }

        this.isMovingAlongPath = false;
    }

    private drawMap() {
        if (!this.tileContainer || !this.gameState) return;
        this.tileContainer.removeChildren();

        const corners: number[] = [];
        for (let i = 0; i < 6; i++) {
            const angle = 2 * Math.PI / 6 * (i + 0.5);
            corners.push(HEX_SIZE * Math.cos(angle), HEX_SIZE * Math.sin(angle));
        }

        this.gameState.mapState.tiles.forEach((tileDto: TileDto) => {
            const hexGraphics = new Graphics();
            const color = tileDto.type === 'WALL' ? 0x333333 : 0xAAAAAA;
            hexGraphics.beginFill(color).lineStyle(1, 0x000000, 0.5).drawPolygon(corners).endFill();
            const pixelPos = hexToPixel(tileDto);
            hexGraphics.position.set(pixelPos.x, pixelPos.y);
            this.tileContainer!.addChild(hexGraphics);
        });
    }

    private drawEntities() {
        if (!this.entityContainer || !this.gameState) return;

        const state = this.gameState;
        const seenEntityIds = new Set<string>();

        state.entities.forEach((entity: EntityStateDto) => {
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

            if (entity.dead) {
                graphics.beginFill(0x333333, 0.6).lineStyle(1, 0x000000, 0.6);
            } else {
                const baseColor = entity.type === 'PLAYER' ? 0x00FF00 : 0xFF0000;
                const finalColor = isDamaged ? 0xFFFFFF : baseColor;
                graphics.beginFill(finalColor, 1.0);
                
                if (isDamaged) graphics.tint = 0xFF0000;
                else graphics.tint = 0xFFFFFF;
                
                if (isSelf) graphics.lineStyle(2, 0xFFFFFF, 1);
                else if (isHoveredEnemy) graphics.lineStyle(2, 0xFFFF00, 1);
            }

            graphics.drawCircle(0, 0, HEX_SIZE * 0.5);
            graphics.endFill();

            const pixelPos = hexToPixel(entity.position);
            if (!gsap.isTweening(graphics.position)) {
                graphics.position.set(pixelPos.x, pixelPos.y);
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

    public animateMovement(event: EntityMovedEvent) {
        const entityGfx = this.entityGraphics.get(event.entityId);
        const entityState = this.gameState?.entities.find((e: EntityStateDto) => e.id === event.entityId);
        if (!entityGfx || !entityState) return;

        const isMyPlayerConfirmingStep = event.entityId === this.gameState?.yourPlayerId && this.isExecutingMove;

        const pixelPos = hexToPixel(event.newPosition);

        gsap.to(entityGfx.position, {
            x: pixelPos.x,
            y: pixelPos.y,
            duration: 0.3,
            ease: 'power1.inOut',
            onComplete: () => {
                entityState.position = event.newPosition;
                entityState.currentAP = event.currentAp ?? entityState.currentAP;
                
                if (isMyPlayerConfirmingStep) {
                    this.movementQueue.shift();
                    this.executeNextMoveStep();
                }
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
        const targetEntity = this.gameState?.entities.find((e: EntityStateDto) => e.id === payload.targetEntityId);
        if (!targetEntity || !this.worldContainer) return;
        
        const pixelPos = hexToPixel(targetEntity.position);

        const style = new TextStyle({
            fontFamily: 'Arial, sans-serif',
            fontSize: 22,
            fontWeight: 'bold',
            fill: '#ff4444',
            stroke: { color: '#000000', width: 4, join: 'round' },
        });

        const damageText = new Text({ text: `-${payload.damageToHp}`, style });
        damageText.anchor.set(0.5);
        damageText.position.set(pixelPos.x, pixelPos.y - HEX_SIZE);
        this.worldContainer.addChild(damageText);
        
        gsap.to(damageText, {
            y: damageText.y - 40,
            alpha: 0,
            duration: 1.2,
            ease: 'power1.out',
            onComplete: () => {
                this.worldContainer?.removeChild(damageText);
                damageText.destroy();
            }
        });
    }
    
    public flashEntity(entityId: string, duration: number = 400) {
        this.damagedEntityInfo = { id: entityId, clearTime: Date.now() + duration };
        this.drawEntities();
    }

    private updateAnimations(ticker: any) {
        const now = Date.now();
        if (this.damagedEntityInfo && now > this.damagedEntityInfo.clearTime) {
            this.damagedEntityInfo = null;
            this.drawEntities();
        }
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