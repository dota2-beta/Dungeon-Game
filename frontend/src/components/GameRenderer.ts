import { gsap } from 'gsap';
import { Application, Container, Graphics, Text, TextStyle } from 'pixi.js';
import { publish } from '../api/websocketService';
import type { ExtendedGameSessionState, PlayerStateDto } from '../context/GameContext';
import { Pathfinder } from '../game/Pathfinder';
import type { AbilityCastedEvent, EntityMovedEvent, EntityStateDto, EntityStatsUpdatedEvent, Hex, TileDto } from '../types/dto';

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

const hexAdd = (a: Hex, b: Hex): Hex => ({ q: a.q + b.q, r: a.r + b.r });

const getHexesInRange = (center: Hex, radius: number): Hex[] => {
    const results: Hex[] = [];
    for (let dx = -radius; dx <= radius; dx++) {
        for (let dy = Math.max(-radius, -dx - radius); dy <= Math.min(radius, -dx + radius); dy++) {
            results.push(hexAdd(center, { q: dx, r: dy }));
        }
    }
    return results;
};

export class GameRenderer {
    private app: Application | null = null;
    private canvasContainer: HTMLDivElement;
    private worldContainer: Container | null = null;
    private tileContainer: Container | null = null;
    private entityContainer: Container | null = null;
    private uiContainer: Container | null = null;

    private gameState: ExtendedGameSessionState | null = null;
    private entityGraphics: Map<string, Graphics> = new Map();
    private damagedEntityInfo: { id: string; clearTime: number } | null = null;

    private pathfinder: Pathfinder | null = null;
    private isMovingAlongPath = false;
    private selectedAbilityId: string | null = null;
    private hoveredHex: Hex | null = null;
    private aoeHighlightGraphics: Graphics | null = null;
    private dispatch: (action: any) => void;

    private lastHoveredEntityId: string | null = null;

    constructor(container: HTMLDivElement, dispatch: (action: any) => void) {
        this.canvasContainer = container;
        this.dispatch = dispatch;
    }

    public async init(initialState: ExtendedGameSessionState) {
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

            this.aoeHighlightGraphics = new Graphics();
            this.uiContainer.addChild(this.aoeHighlightGraphics);
            
            this.app.ticker.add(this.gameLoop.bind(this));

            this.app.stage.addChild(this.worldContainer);
            this.worldContainer.position.set(canvasWidth / 2, canvasHeight / 2);

            this.canvasContainer.innerHTML = '';
            this.canvasContainer.appendChild(this.app.canvas);

            this.app.ticker.add(this.updateAnimations.bind(this));
            
            this.gameState = initialState; 
            this.setupEventHandlers();
            
            this.pathfinder = new Pathfinder(initialState.mapState);
            
            this.drawMap();
            this.drawEntities();

            const player = initialState.entities.find(e => e.id === initialState.yourPlayerId);
        
            if (player) {
                this.centerCameraOn(player.position, false);
            }
        }
    }

    public update(newState: ExtendedGameSessionState) {
        if (!this.app) {
            if (newState.mapState?.tiles.length > 0) {
                this.init(newState);
            }
            return;
        }
        
        this.gameState = newState;
        this.selectedAbilityId = newState.selectedAbility?.abilityTemplateId || null;
        
        this.drawEntities();
    }

    public drawAbilityHighlight(state: ExtendedGameSessionState, hoveredHex: Hex | null) {
        if (!this.aoeHighlightGraphics) return;
        this.aoeHighlightGraphics.clear();

        const player = state.entities.find(e => e.id === state.yourPlayerId);
        const selectedAbilityId = state.selectedAbility?.abilityTemplateId;

        if (selectedAbilityId && hoveredHex && player) {
            const abilityTemplate = state.abilities.find(
                (a) => a.templateId === selectedAbilityId
            );

            if (!abilityTemplate) {
                return;
            }

            const radius = abilityTemplate.areaOfEffectRadius;
            const range = abilityTemplate.range;
            const distance = hexDistance(player.position, hoveredHex);
            const color = distance <= range ? 0x00FF00 : 0xFF0000;
            const hexesToHighlight = getHexesInRange(hoveredHex, radius);
            
            const corners = Array.from({ length: 6 }, (_, i) => {
                const angle = 2 * Math.PI / 6 * (i + 0.5);
                return [HEX_SIZE * Math.cos(angle), HEX_SIZE * Math.sin(angle)];
            }).flat();

            for (const hex of hexesToHighlight) {
                const pixelPos = hexToPixel(hex);
                this.aoeHighlightGraphics
                    .poly(corners.map((point, index) => index % 2 === 0 ? point + pixelPos.x : point + pixelPos.y))
                    .fill({ color: color, alpha: 0.3 })
                    .stroke({ width: 1, color: color, alpha: 0.7 });
            }
        }
    }
    
    private setupEventHandlers() {
        if (!this.app || !this.worldContainer) return;

        this.worldContainer.removeAllListeners();
        if (this.app.canvas.oncontextmenu) {
            this.app.canvas.oncontextmenu = null;
        }

        this.app.canvas.addEventListener('contextmenu', (e) => {
            e.preventDefault();

            if (!this.gameState || !this.worldContainer) return;
            
            const worldPos = this.worldContainer.toLocal({ x: e.offsetX, y: e.offsetY });
            const targetHex = pixelToHex(worldPos.x, worldPos.y);
            
            const targetEntity = this.gameState.entities.find(entity => 
                !entity.dead && 
                entity.position.q === targetHex.q && 
                entity.position.r === targetHex.r
            ) as PlayerStateDto | undefined;

            const player = this.gameState.entities.find(e => e.id === this.gameState!.yourPlayerId);

            if (targetEntity && targetEntity.type === 'PLAYER' && targetEntity.id !== player?.id) {
                this.dispatch({ 
                    type: 'OPEN_CONTEXT_MENU', 
                    payload: { 
                        x: e.clientX, 
                        y: e.clientY, 
                        targetPlayer: targetEntity 
                    } 
                });
            } else {
                this.dispatch({ type: 'CLOSE_CONTEXT_MENU' });
            }
        });

        this.worldContainer.interactive = true;
        this.worldContainer.on('pointerdown', (event) => {
            if (event.nativeEvent.button === 2) {
                return;
            }

            if (!this.gameState) return;
            const player = this.gameState.entities.find(e => e.id === this.gameState!.yourPlayerId);
            if (!player) return;
        
            const pos = event.data.getLocalPosition(this.worldContainer!);
            const targetHex = pixelToHex(pos.x, pos.y);
        
            this.dispatch({ type: 'CLOSE_CONTEXT_MENU' });

            if (this.selectedAbilityId) {
                publish(`/app/session/${this.gameState.sessionId}/action`, {
                    actionType: 'CAST_SPELL',
                    abilityId: this.selectedAbilityId,
                    targetHex: targetHex
                });
            } else {
                const targetEntity = this.gameState.entities.find(e => 
                    !e.dead && e.position.q === targetHex.q && e.position.r === targetHex.r 
                );
                
                if (targetEntity && targetEntity.id !== player.id) {
                    //console.log(`Target is not an ally. Sending ATTACK command to ${targetEntity.id}`);
                    publish(`/app/session/${this.gameState.sessionId}/action`, { actionType: 'ATTACK', targetId: targetEntity.id });
                } else {
                    this.handleMoveRequest(player, targetHex);
                }
            }
        });

        this.worldContainer.on('pointermove', (event) => {
            if (!this.worldContainer || !this.gameState) return;
            const pos = event.data.getLocalPosition(this.worldContainer);
            const currentHoveredHex = pixelToHex(pos.x, pos.y);
            const hoveredEntity = this.gameState.entities.find(e => 
                !e.dead && e.position.q === currentHoveredHex.q && e.position.r === currentHoveredHex.r
            );
            const currentHoveredId = hoveredEntity ? hoveredEntity.id : null;
    
            if (currentHoveredId !== this.lastHoveredEntityId) {
                this.dispatch({ type: 'SET_HOVERED_ENTITY', payload: currentHoveredId });
                this.lastHoveredEntityId = currentHoveredId;
            }
    
            if (!this.hoveredHex || this.hoveredHex.q !== currentHoveredHex.q || this.hoveredHex.r !== currentHoveredHex.r) {
                this.hoveredHex = currentHoveredHex;
                if(this.gameState) this.drawAbilityHighlight(this.gameState, this.hoveredHex);
            }
            
            this.canvasContainer.style.cursor = this.selectedAbilityId ? 'crosshair' : 'default';
        });
    }
    
    private handleMoveRequest(player: EntityStateDto, targetHex: Hex) {
        if (!this.pathfinder || !this.gameState) return; 
    
        const path = this.pathfinder.findPath(player.position, targetHex, this.gameState.entities);
        
        if (path.length > 1) {
            this.executeMovePath(path);
        } else {
            //console.warn("No path found or target is the same as start.");
        }
    }

    public playAbilityAnimation(payload: AbilityCastedEvent) {
        const casterGfx = this.entityGraphics.get(payload.casterId);
        if (!casterGfx || !this.worldContainer) return;
        
        const targetPixelPos = hexToPixel(payload.targetHex);

        const projectile = new Graphics();
        projectile.beginFill(0xff00ff).drawCircle(0, 0, 8).endFill();
        projectile.position.set(casterGfx.position.x, casterGfx.position.y);
        this.worldContainer.addChild(projectile);

        gsap.to(projectile, {
            x: targetPixelPos.x,
            y: targetPixelPos.y,
            duration: 0.5,
            ease: 'power1.in',
            onComplete: () => {
                this.worldContainer?.removeChild(projectile);
                projectile.destroy();
            }
        });
    }

    private async executeMovePath(path: Hex[]) {
        if (this.isMovingAlongPath) return;
        this.isMovingAlongPath = true;

        for (let i = 1; i < path.length; i++) {
            const player = this.gameState!.entities.find((e: EntityStateDto) => e.id === this.gameState!.yourPlayerId)!;
            if (player.state === 'COMBAT' && player.currentAP < 1) {
                //console.log("Out of AP, stopping movement.");
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

        const yourPlayerTeamId = state.entities.find(e => e.id === state.yourPlayerId)?.teamId;

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
            const isDamaged = this.damagedEntityInfo?.id === entity.id;

            graphics.lineStyle(0, 0, 0); 

            if (entity.dead) {
                graphics.beginFill(0x333333, 0.6).lineStyle(1, 0x000000, 0.6);
            } else {
                const baseColor = entity.type === 'PLAYER' ? 0x00FF00 : 0xFF0000;
                graphics.beginFill(baseColor, 1.0);
                
                if (isDamaged) {
                    graphics.tint = 0xFF0000;
                } else {
                    graphics.tint = 0xFFFFFF;
                }
                
                if (isSelf) {
                    graphics.lineStyle(2, 0xFFFFFF, 1);
                } else if (entity.teamId && entity.teamId === yourPlayerTeamId) {
                    graphics.lineStyle(2, 0x00BFFF, 1);
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

    public animateMovement(event: EntityMovedEvent) {
        const entityGfx = this.entityGraphics.get(event.entityId);
        const entityState = this.gameState?.entities.find((e: EntityStateDto) => e.id === event.entityId);
        if (!entityGfx || !entityState) return;

        const pixelPos = hexToPixel(event.newPosition);

        gsap.to(entityGfx.position, {
            x: pixelPos.x,
            y: pixelPos.y,
            duration: 0.3,
            ease: 'power1.inOut',
            onComplete: () => {
                entityState.position = event.newPosition;
                if (typeof event.currentAP === 'number') {
                    entityState.currentAP = event.currentAP;
                }
                
                if (event.entityId === this.gameState?.yourPlayerId) {
                    this.centerCameraOn(event.newPosition, true);
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
        if (!this.gameState) return;
        this.damagedEntityInfo = { id: entityId, clearTime: Date.now() + duration };
        this.drawEntities();
    }

    private updateAnimations() {
        if (this.damagedEntityInfo && Date.now() > this.damagedEntityInfo.clearTime) {
            if (!this.gameState) return; 
            this.damagedEntityInfo = null;
            this.drawEntities();
        }
    }

    private gameLoop(): void {
        if (!this.aoeHighlightGraphics || !this.gameState) {
            if (this.aoeHighlightGraphics) this.aoeHighlightGraphics.clear();
            return;
        }
    
        this.aoeHighlightGraphics.clear();
    
        const player = this.gameState.entities.find(e => e.id === this.gameState!.yourPlayerId);
    
        if (this.selectedAbilityId && this.hoveredHex && player) {
            
            const abilityTemplate = this.gameState.abilities.find(
                (a) => a.templateId === this.selectedAbilityId
            );
    
            if (!abilityTemplate) {
                return;
            }
    
            const radius = abilityTemplate.areaOfEffectRadius;
            const range = abilityTemplate.range;
            
            const distance = hexDistance(player.position, this.hoveredHex);
            const color = distance <= range ? 0x00FF00 : 0xFF0000;
            const hexesToHighlight = getHexesInRange(this.hoveredHex, radius);
    
            const corners: number[] = [];
            for (let i = 0; i < 6; i++) {
                const angle = 2 * Math.PI / 6 * (i + 0.5); 
                corners.push(HEX_SIZE * Math.cos(angle), HEX_SIZE * Math.sin(angle));
            }
    
            for (const hex of hexesToHighlight) {
                const pixelPos = hexToPixel(hex);
                this.aoeHighlightGraphics
                    .poly(corners.map((point, index) => index % 2 === 0 ? point + pixelPos.x : point + pixelPos.y))
                    .fill({ color: color, alpha: 0.3 })
                    .stroke({ width: 1, color: color, alpha: 0.7 });
            }
        }
    }

    private centerCameraOn(hex: Hex, smooth: boolean = false): void {
        if (!this.app || !this.worldContainer) {
            return;
        }
        const pixelPos = hexToPixel(hex);

        const targetX = (this.app.screen.width / 2) - pixelPos.x;
        const targetY = (this.app.screen.height / 2) - pixelPos.y;

        if (smooth) {
            gsap.to(this.worldContainer.position, {
                x: targetX,
                y: targetY,
                duration: 0.5,
                ease: 'power2.out', 
            });
        } else {
            this.worldContainer.position.set(targetX, targetY);
        }
    }

    public destroy() {
        if (this.app) {
            this.app.ticker.stop();
            gsap.globalTimeline.clear();
            this.app.destroy(true, { children: true, texture: true });
            this.app = null;
            this.canvasContainer.innerHTML = '';
            //console.log("GameRenderer destroyed.");
        }
    }
}