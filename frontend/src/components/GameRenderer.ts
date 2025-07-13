import { Application, Container, Graphics, Text, TextStyle } from 'pixi.js';
import type { GameSessionStateDto, PlayerAction, EntityStateDto, EntityStatsUpdatedEvent, Hex } from '../types/dto';
import { publish } from '../api/websocketService';
import { gsap } from 'gsap';

// =================================================================
// Константы и вспомогательные функции для гексов
// =================================================================

const HEX_SIZE = 24; // Размер гекса в пикселях (от центра до угла)

/**
 * Преобразует гексагональные координаты в пиксельные на экране (для ориентации "острым концом вверх").
 */
const hexToPixel = (hex: Hex): { x: number; y: number } => {
    const x = HEX_SIZE * (Math.sqrt(3) * hex.q + (Math.sqrt(3) / 2) * hex.r);
    const y = HEX_SIZE * (3/2 * hex.r);
    return { x, y };
};

/**
 * Преобразует пиксельные координаты в (возможно, дробные) гексагональные.
 */
const pixelToHex = (x: number, y: number): Hex => {
    const q = (Math.sqrt(3)/3 * x - 1/3 * y) / HEX_SIZE;
    const r = (2/3 * y) / HEX_SIZE;
    return hexRound({ q, r });
};

/**
 * Округляет дробные гексагональные координаты до ближайшего целого гекса.
 */
const hexRound = (frac: { q: number; r: number }): Hex => {
    const s_frac = -frac.q - frac.r;
    let q = Math.round(frac.q);
    let r = Math.round(frac.r);
    let s = Math.round(s_frac);

    const q_diff = Math.abs(q - frac.q);
    const r_diff = Math.abs(r - frac.r);
    const s_diff = Math.abs(s - s_frac);

    if (q_diff > r_diff && q_diff > s_diff) {
        q = -r - s;
    } else if (r_diff > s_diff) {
        r = -q - s;
    }

    return { q, r };
};

const hexSubtract = (a: Hex, b: Hex): Hex => ({ q: a.q - b.q, r: a.r - b.r });

const hexDistance = (a: Hex, b: Hex): number => {
    const vec = hexSubtract(a, b);
    const s = -vec.q - vec.r;
    return (Math.abs(vec.q) + Math.abs(vec.r) + Math.abs(s)) / 2;
};

// =================================================================
// Основной класс рендерера
// =================================================================

interface DamageTextAnimation {
    text: Text;
    startTime: number;
    duration: number;
}

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
    private activeDamageTexts: DamageTextAnimation[] = [];
    private damagedEntityInfo: { id: string; clearTime: number } | null = null;

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
            
            this.worldContainer.addChild(this.tileContainer, this.entityContainer);
            this.app.stage.addChild(this.worldContainer, this.uiContainer);

            this.worldContainer.position.set(canvasWidth / 2, canvasHeight / 2);

            this.canvasContainer.innerHTML = '';
            this.canvasContainer.appendChild(this.app.canvas);

            this.setupEventHandlers();
            this.app.ticker.add(this.updateAnimations.bind(this));
            
            console.log('%cGameRenderer: Init complete. Drawing map and entities for the first time.', 'color: blue; font-weight: bold;');
            this.drawMap();
            this.drawEntities();
        }
    }

    public update(newState: GameSessionStateDto) {
        const isFirstUpdate = !this.gameState;
        this.gameState = newState;

        if (!this.app && newState.mapState?.tiles.length > 0) {
            this.init(newState);
        } else if (this.app) {
            if (isFirstUpdate) {
                this.drawMap();
            }
            this.drawEntities();
        }
    }
    
    private setupEventHandlers() {
        if (!this.app || !this.worldContainer) return;
        this.worldContainer.interactive = true;
        
        this.worldContainer.on('pointerdown', (event) => {
            if (!this.gameState?.sessionId || !this.app) return;
            
            const pos = event.data.getLocalPosition(this.worldContainer!);
            const targetHex = pixelToHex(pos.x, pos.y);
            
            const targetEntity = this.gameState.entities.find((e) => e.position.q === targetHex.q && e.position.r === targetHex.r);
            
            let action: PlayerAction;
            if (targetEntity && targetEntity.id !== this.gameState.yourPlayerId && !targetEntity.isDead) {
                action = { actionType: 'ATTACK', targetId: targetEntity.id };
            } else {
                action = { actionType: 'MOVE', targetHex: targetHex };
            }
            publish(`/app/session/${this.gameState.sessionId}/action`, action);
        });

        this.worldContainer.on('pointermove', (event) => {
            if (!this.app || !this.gameState) return;
            const pos = event.data.getLocalPosition(this.worldContainer!);
            const hoveredHex = pixelToHex(pos.x, pos.y);

            const targetEntity = this.gameState.entities.find((e) => e.position.q === hoveredHex.q && e.position.r === hoveredHex.r);
            const player = this.gameState.entities.find(e => e.id === this.gameState!.yourPlayerId);
            
            let newHoverId: string | null = null;

            if (player && targetEntity && targetEntity.id !== player.id && !targetEntity.isDead) {
                const playerAttackRange = (player as any).attackRange || 1;
                if (hexDistance(player.position, targetEntity.position) <= playerAttackRange) {
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

    private drawMap() {
        if (!this.tileContainer || !this.gameState) return;
        this.tileContainer.removeChildren();

        const corners: number[] = [];
        for (let i = 0; i < 6; i++) {
            const angle = 2 * Math.PI / 6 * (i + 0.5);
            corners.push(HEX_SIZE * Math.cos(angle), HEX_SIZE * Math.sin(angle));
        }

        this.gameState.mapState.tiles.forEach(tileDto => {
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

            if (entity.isDead) {
                graphics.beginFill(0x333333, 0.6).lineStyle(1, 0x000000, 0.6);
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
                }
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
        damageText.position.set(targetGfx.x, targetGfx.y - HEX_SIZE);
        this.uiContainer.addChild(damageText);
        
        this.activeDamageTexts.push({ text: damageText, startTime: Date.now(), duration: 1200 });
    }
    
    public flashEntity(entityId: string, duration: number = 400) {
        this.damagedEntityInfo = { id: entityId, clearTime: Date.now() + duration };
        this.drawEntities();
    }

    private updateAnimations(ticker: any) {
        const now = Date.now();
        const delta = ticker.deltaMS;

        if (this.damagedEntityInfo && now > this.damagedEntityInfo.clearTime) {
            this.damagedEntityInfo = null;
            this.drawEntities();
        }

        this.activeDamageTexts = this.activeDamageTexts.filter(anim => {
            const elapsedTime = now - anim.startTime;
            if (elapsedTime >= anim.duration) {
                this.uiContainer?.removeChild(anim.text);
                anim.text.destroy();
                return false;
            }
            anim.text.y -= 0.8 * (delta / 16.67);
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