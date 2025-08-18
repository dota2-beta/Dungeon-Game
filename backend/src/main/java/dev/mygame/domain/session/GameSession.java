package dev.mygame.domain.session;

//import dev.mygame.game.model.map.Map;
import dev.mygame.config.StandartEntityGameSettings;
import dev.mygame.domain.event.CombatEventListener;
import dev.mygame.domain.event.SessionEvent;
import dev.mygame.domain.model.Entity;
import dev.mygame.domain.model.GameObject;
import dev.mygame.domain.model.Monster;
import dev.mygame.domain.model.Player;
import dev.mygame.domain.model.map.*;
import dev.mygame.dto.websocket.event.*;
import dev.mygame.dto.websocket.request.PeaceProposalEvent;
import dev.mygame.dto.websocket.request.PeaceProposalResultEvent;
import dev.mygame.dto.websocket.response.*;
import dev.mygame.enums.EntityStateType;
import dev.mygame.mapper.EntityMapper;
import dev.mygame.mapper.GameSessionMapper;
import dev.mygame.mapper.context.MappingContext;
import dev.mygame.service.AIService;
import dev.mygame.service.AbilityService;
import dev.mygame.service.FactionService;
import dev.mygame.service.GameEventNotifier;
import dev.mygame.service.internal.*;
import dev.mygame.enums.ActionType;
import dev.mygame.enums.CombatOutcome;
import dev.mygame.domain.event.GameSessionEndListener;
import lombok.Builder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Data
@Builder
public class GameSession implements CombatEventListener {
    private String sessionID;
    private final ApplicationEventPublisher eventPublisher;
    private final GameEventNotifier notifier;
    private final StandartEntityGameSettings standartEntityGameSettings;

    @Builder.Default
    private Map<String, Entity> entities = new ConcurrentHashMap<>();
    private Map<String, GameObject> gameObjects;

    private GameMapHex gameMap;

    @Builder.Default
    private Map<String, CombatInstance> activeCombats = new ConcurrentHashMap<>();

    private final FactionService factionService;
    private final AbilityService abilityService;
    private final AIService aiService;

    private final GameSessionMapper gameSessionMapper;
    private final EntityMapper entityMapper;

    // Ключ - ID приглашенного игрока, Значение - ID команды, в которую его приглашают.
    @Builder.Default
    private Map<String, String> pendingInvites = new ConcurrentHashMap<>();

    // Map<ID комбата, Map<Id юзера, его ответ на приглашение>>
    @Builder.Default
    private Map<String, Map<String, Boolean>> peacefulAgreements =  new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler;
    private static final Logger log = LoggerFactory.getLogger(GameSession.class);

    @Builder.Default
    private List<GameSessionEndListener> endListeners = new ArrayList<>();;

    /**
     * Метод для добавления новых сущностей в сессию
     * @param entity объект сущности
     */
    public void addEntity(Entity entity) {
        if(!entities.containsKey(entity.getId())) {
            entities.put(entity.getId(), entity);
        }
    }

    public void removeEntity(Entity entity) {
        entities.remove(entity.getId());
    }

    public void publishEvent(Object payload) {
        eventPublisher.publishEvent(new SessionEvent<>(this, this, payload));
    }

    /**
     * Отправляет полное состояние текущей игровой сессии одному конкретному пользователю.
     * @param userId ID пользователя-получателя.
     */
    public void sendInitialStateToPlayer(String userId) {
        MappingContext context = new MappingContext(userId);
        GameSessionStateDto sessionState = gameSessionMapper.toGameSessionState(this, context);

        notifier.notifyFullGameState(userId, this.getSessionID(), sessionState);
    }

    /**
     * Центральный метод для обработки всех событий, которые инициирует какая-либо сущность
     * @param entityId ID сущности-инициатора.
     * @param action   Объект, описывающий действие.
     */
    public void handleEntityAction(String entityId, EntityAction action) {
        Entity entity = entities.get(entityId);

        if (entity == null || entity.isDead()) {
            if (entity instanceof Player) {
                sendErrorMessageToPlayer(
                        (Player) entity,
                        "You cannot perform actions while you are dead.",
                        "PLAYER_IS_DEAD"
                );
            }
            return;
        }

        if (entity.getState() == EntityStateType.COMBAT) {
            CombatInstance combat = findCombatForEntity(entityId);

            if (combat != null) {
                if (!combat.getCurrentTurnEntityId().equals(entityId)) {
                    if (entity instanceof Player) {
                        sendErrorMessageToPlayer((Player) entity, "It's not your turn!", "NOT_YOUR_TURN");
                    }
                    return;
                }
            } else {
                return;
            }
        }

        ActionType actionType = action.getActionType();
        switch (actionType) {
            case MOVE:
                entityMove(entityId, action.getTargetHex());
                break;
            case ATTACK:
                entityAttack(entityId, action.getTargetId());
                break;
//            case INTERACT:
//                entityInteract(entityId, action.getInteractId());
//                break;
//            case USE_ITEM:
//                entityUseItem(entityId, action.getItemId() , action.getPoint());
//                break;
            case CAST_SPELL: {
                entityUseAbility(entityId, action);
                break;
            }

            case END_TURN:
                endTurn(entityId);
                break;
        }
    }

    private void entityUseAbility(String entityId, EntityAction action) {
        Entity caster = entities.get(entityId);
        if (caster == null) {
            return;
        }

        Optional<AbilityInstance> abilityInstanceOpt = caster.getAbilities().stream()
                .filter(instance -> instance.getTemplate().getTemplateId().equals(action.getAbilityId()))
                .findFirst();

        if (abilityInstanceOpt.isEmpty()) {
            return;
        }

        AbilityInstance abilityToUse = abilityInstanceOpt.get();
        Hex targetHex = action.getTargetHex();

        AbilityUseResult result = abilityService.useAbility(this, caster, abilityToUse, targetHex);

        switch (result.getSuccess()) {
            case OUT_OF_RANGE: {
                notifier.notifyError((Player) caster, "Target is out of range", "400");
                return;
            }
            case ON_COOLDOWN: {
                notifier.notifyError((Player) caster, "Ability on cooldown", "400");
                return;
            }
            case NOT_ENOUGH_AP: {
                notifier.notifyError((Player) caster, "You haven't enough action point", "400");
                return;
            }
            case CASTER_IS_DEAD: {
                notifier.notifyError((Player) caster, "You are dead", "400");
                return;
            }
            case INVALID_TARGET: {
                notifier.notifyError((Player) caster, "Invalid target", "400");
                return;
            }
        }

        publishCasterStateUpdate(result.getCaster());
        AbilityCastedEvent castedEvent = AbilityCastedEvent.builder()
                .casterId(caster.getId())
                .abilityTemplateId(result.getAbilityTemplateId())
                .targetHex(targetHex)
                .build();
        publishEvent(castedEvent);

        for (EffectResult effectResult : result.getEffectsResults()) {
            if (effectResult instanceof DamageResult damageResult) {
                Entity target = entities.get(damageResult.getEntityId());
                if (target == null) continue;

                EntityStatsUpdatedEvent event = EntityStatsUpdatedEvent.builder()
                        .targetEntityId(target.getId())
                        .damageToHp(damageResult.getDamageToHp())
                        .isDead(damageResult.isDead())
                        .absorbedByArmor(damageResult.getAbsorbedByArmor())
                        .currentDefense(target.getDefense())
                        .currentHp(target.getCurrentHp())
                        .build();
                publishEvent(event);
            }

            if(effectResult instanceof HealResult healResult) {
                Entity target = entities.get(healResult.getEntityId());
                if (target == null) continue;

                EntityStatsUpdatedEvent event = EntityStatsUpdatedEvent.builder()
                    .targetEntityId(target.getId())
                    .currentHp(healResult.getNewCurrentHp())
                    .healToHp(healResult.getActualHealedAmount())
                    .isDead(false)
                    .build();
                publishEvent(event);
            }
        }

        // если способность дамажит, то выделем тех, кто получил дамаг, и проверяем, являются ли они противниками
        // если да, то начинаем бой, иначе - ничего
        if (!abilityToUse.getTemplate().hasDamageEffect()) {
            return;
        }

        List<Entity> affectedTargets = findTargetsInArea(targetHex, abilityToUse.getTemplate().getAreaOfEffectRadius());

        Optional<Entity> hostileTargetOpt = affectedTargets.stream()
                .filter(target -> factionService.areEnemies(caster, target))
                .findFirst();

        if (hostileTargetOpt.isEmpty()) {
            return;
        }

        Entity hostileTarget = hostileTargetOpt.get();
        // начинаем новый бой
        if (caster.getState() == EntityStateType.EXPLORING && hostileTarget.getState() == EntityStateType.EXPLORING) {
            log.info("Combat initiated by ability...");
            List<Entity> participants = findNearbyAlliesAndEnemies(caster, hostileTarget);
            startCombat(participants);
        }
        // присоединяемся к бою
        else if (caster.getState() == EntityStateType.EXPLORING && hostileTarget.getState() == EntityStateType.COMBAT) {
            log.info("{} joins an existing combat by casting a spell!", caster.getName());
            CombatInstance combat = findCombatForEntity(hostileTarget.getId());
            if (combat != null) {
                List<Entity> joiningGroup = findNearbyAlliesAndEnemies(caster, hostileTarget);
                combat.addParticipantsToCombat(joiningGroup);
            }
        }

    }

    /**
     * Завершает текущий ход для сущности, находящейся в бою.
     * <p>
     * Метод находит бой, в котором участвует сущность, и делегирует ему
     * команду на переход к следующему ходу. Если сущность не находится в бою,
     * действие безопасно игнорируется.
     *
     * @param entityId ID сущности, завершающей ход.
     */
    public void endTurn(String entityId) {
        Entity entity = entities.get(entityId);
        if (entity.getState() != EntityStateType.COMBAT) {
            log.warn("Entity {} tried to end turn while not in combat.", entity.getName());
            return;
        }

        CombatInstance combat = findCombatForEntity(entityId);
        if (combat != null) {
            log.info("Entity {} ends their turn.", entity.getName());
            combat.proceedToNextTurn();
        }
    }

    private CombatInstance findCombatForEntity(String entityId) {
        for(CombatInstance combat : activeCombats.values()) {
            boolean isInCombat = combat.getTeams().values().stream()
                    .flatMap(Set::stream)
                    .anyMatch(entity -> entity.getId().equals(entityId));
            if(isInCombat) {
                return combat;
            }
        }
        return null;
    }

    private void entityMove(String entityId, Hex targetHex) {
        Entity entity = entities.get(entityId);
        if (entity == null) {
            return;
        }
        int distance = entity.getPosition().distanceTo(targetHex);
        if (distance != 1) {
            if (entity instanceof Player)
                sendErrorMessageToPlayer((Player) entity, "You can only move to adjacent hexes.", "MOVE_INVALID_DISTANCE");
            return;
        }

        Tile targetTile = this.gameMap.getTile(targetHex);
        if (targetTile == null || !targetTile.isPassable()) {
            if (entity instanceof Player)
                sendErrorMessageToPlayer((Player) entity, "You can't move there.", "TILE_NOT_PASSABLE");
            return;
        }

        if (targetTile.isOccupied()) {
            if (entity instanceof Player)
                sendErrorMessageToPlayer((Player) entity, "This tile is occupied.", "TILE_OCCUPIED");
            return;
        }

        int moveCost = standartEntityGameSettings.getDefaultMovementCost();
        if (entity.getState() == EntityStateType.COMBAT && entity.getCurrentAP() < moveCost) {
            if (entity instanceof Player)
                sendErrorMessageToPlayer((Player) entity, "Not enough Action Points to move.", "NOT_ENOUGH_AP");
            return;
        }

        this.gameMap.getTile(entity.getPosition()).setOccupiedById(null);

        entity.setPosition(targetHex);

        this.gameMap.getTile(targetHex).setOccupiedById(entity.getId());

        if (entity.getState() == EntityStateType.COMBAT) {
            entity.setCurrentAP(entity.getCurrentAP() - moveCost);
        }

        EntityMovedEvent movedEvent = EntityMovedEvent.builder()
                .entityId(entity.getId())
                .newPosition(entity.getPosition())
                .currentAP(entity.getCurrentAP())
                .pathToAnimate(List.of(targetHex))
                .reachedTarget(true)
                .build();

        publishEvent( movedEvent);
        checkForCombatStart(entity);
    }

    private void entityAttack(String attackerId, String targetId) {
        Entity attacker = entities.get(attackerId);
        Entity target = entities.get(targetId);

        if (target == null || target.isDead()) {
            if (attacker instanceof Player) {
                sendErrorMessageToPlayer((Player) attacker, "Target is already dead.", "TARGET_IS_DEAD");
            }
            return;
        }

        if (!isTargetInRange(attacker, target)) {
            if (attacker instanceof Player) {
                sendErrorMessageToPlayer((Player) attacker, "Target is out of range.", "TARGET_OUT_OF_RANGE");
            }
            return;
        }

        boolean isAttackerInCombat = attacker.getState() == EntityStateType.COMBAT;
        boolean isTargetInCombat = target.getState() == EntityStateType.COMBAT;

        if (!isAttackerInCombat && !isTargetInCombat) {
            if(factionService.areEnemies(attacker, target)) {
                log.info(">>> SERVER: Initiating new combat via entityAttack. Attacker: {}, Target: {}", attacker.getId(), target.getId());

                DamageResult damageResult = target.takeDamage(attacker.getAttack());
                publishAttackEvents(attacker, target, damageResult);

                List<Entity> participants = findNearbyAlliesAndEnemies(attacker, target);

                startCombat(participants);

            } else if(Objects.equals(attacker.getTeamId(), target.getTeamId())) {
                DamageResult damageResult = target.takeDamage(attacker.getAttack());
                publishAttackEvents(attacker, target, damageResult);
            }
            return;
        }

        if(!isAttackerInCombat && isTargetInCombat && factionService.areEnemies(attacker, target)) {
            log.info("{} joins an existing combat by attacking {}!", attacker.getName(), target.getName());
            DamageResult damageResult = target.takeDamage(attacker.getAttack());
            publishAttackEvents(attacker, target, damageResult);
            CombatInstance combat = findCombatForEntity(target.getId());
            if(combat != null) {
                combat.addParticipantsToCombat(findNearbyAlliesAndEnemies(attacker, target));
            }
        }

        if (isAttackerInCombat) {
            if (attacker.getCurrentAP() < standartEntityGameSettings.getDefaultAttackCost()) {
                if (attacker instanceof Player) {
                    sendErrorMessageToPlayer((Player) attacker, "Not enough Action Points to attack.", "NOT_ENOUGH_AP");
                }
                return;
            }
            DamageResult damageResult = target.takeDamage(attacker.getAttack());
            attacker.setCurrentAP(attacker.getCurrentAP() - standartEntityGameSettings.getDefaultAttackCost());

            publishAttackEvents(attacker, target, damageResult);
        }
    }


    private void publishAttackEvents(Entity attacker, Entity target, DamageResult damageResult) {
        EntityAttackEvent attackEvent = new EntityAttackEvent(
                attacker.getId(),
                target.getId(),
                attacker.getAttack(),
                attacker.getCurrentAP()
        );
        publishEvent(attackEvent);

        EntityStatsUpdatedEvent statsUpdateEvent = EntityStatsUpdatedEvent.builder()
                .targetEntityId(target.getId())
                .damageToHp(damageResult.getDamageToHp())
                .currentHp(target.getCurrentHp())
                .absorbedByArmor(damageResult.getAbsorbedByArmor())
                .currentDefense(target.getDefense())
                .isDead(target.isDead())
                .build();
        publishEvent(statsUpdateEvent);
    }

    /**
     * Находит и возвращает список всех сущностей, находящихся в некотором
     * радиусе от заданной точки.
     *
     * @param centerHex центр области поиска.
     * @param radius    радиус области в гексах (0 = только центральный гекс).
     * @return Список найденных сущностей. Возвращает пустой список, если никто не найден.
     */
    public List<Entity> findTargetsInArea(Hex centerHex, int radius) {
        if (radius == 0) {
            Entity targetInCenter = this.getEntityAt(centerHex);
            return (targetInCenter != null) ? List.of(targetInCenter) : List.of();
        }

        return this.getEntities().values().stream()
                .filter(entity -> entity.getPosition().distanceTo(centerHex) <= radius)
                .collect(Collectors.toList());
    }

    private List<Entity> findNearbyAlliesAndEnemies(Entity attacker, Entity target) {
        final int COMBAT_JOIN_RADIUS = standartEntityGameSettings.getDefaultCheckRadius();

        return entities.values().stream()
                .filter(entity -> entity.getState() == EntityStateType.EXPLORING)
                .filter(entity -> entity.getPosition().distanceTo(attacker.getPosition()) <= COMBAT_JOIN_RADIUS ||
                        entity.getPosition().distanceTo(target.getPosition()) <= COMBAT_JOIN_RADIUS)
                .filter(entity -> Objects.equals(entity.getTeamId(), attacker.getTeamId()) ||
                        Objects.equals(entity.getTeamId(), target.getTeamId()) ||
                        entity.equals(attacker) || entity.equals(target))
                .toList();
    }

    private boolean isTargetInRange(Entity attacker, Entity target) {
        if(attacker == null || target == null)
            return false;

        int attackRange = attacker.getAttackRange();

        int distance = attacker.getPosition().distanceTo(target.getPosition());
        return attackRange >= distance;
    }

    public void addGameSessionEndListener(GameSessionEndListener listener) {
        this.endListeners.add(listener);
    }

    public void removeGameSessionEndListener(GameSessionEndListener listener) {
        this.endListeners.remove(listener);
    }

    /**
     * Оповещает всех зарегистрированных слушателей о завершении этой игровой сессии.
     */
    public void notifyGameSessionEndListeners() {
        for(GameSessionEndListener listener : this.endListeners) {
            listener.onGameSessionEnd(this);
        }
    }

    /**
     * Метод для получения игрока по его веб-сокет соединению
     * @param websocketSessionId ID соединения, по которому нужно найти игрока.
     * @return объект {@link Player} найденного игрока или {@code null}.
     */
    public Player getPlayerByWebsocketSessionId(String websocketSessionId) {
        for(Entity entity : entities.values())
            if(entity instanceof Player player)
                if(player.getWebsocketSessionId().equals(websocketSessionId))
                    return player;
        return null;
    }

    /**
     * Метод для проверки необходимости начала боя после перемещения.
     * <p>
     * Бой начинается, когда сущность заходит в зону "агра".
     * @param movedEntity ID сущности, которая совершила перемещение
     */
    public void checkForCombatStart(Entity movedEntity) {
        if(movedEntity.getState() == EntityStateType.COMBAT)
            return;

        final int MAX_CHECK_RADIUS = 10;

        List<Entity> nearbyEntities = findAllEntitiesInRadius(movedEntity.getId(), MAX_CHECK_RADIUS);
        if (nearbyEntities.isEmpty())
            return;

        boolean isEnemyPresent = false;
        String existingCombatId = null;
        for(Entity nearbyEntity : nearbyEntities)
            if(factionService.areEnemies(nearbyEntity, movedEntity)) {
                if(nearbyEntity.getState() == EntityStateType.EXPLORING) {
                    int distance = movedEntity.getPosition().distanceTo(nearbyEntity.getPosition());
                    if (distance <= movedEntity.getAggroRadius() || distance <= nearbyEntity.getAggroRadius()) {
                        // бой будет начинаться для ВСЕХ юнитов, которые находятся рядом, а не только для "врагов"
                        // потом можно создать метод для окончания боя по согласию всех участников
                        isEnemyPresent = true;
                    }
                } else {
                    CombatInstance combat = findCombatForEntity(nearbyEntity.getId());
                    if (combat != null) {
                        existingCombatId = combat.getCombatId();
                        break;
                    }
                }
            }
        if (!isEnemyPresent && existingCombatId == null) {
            return;
        }

        List<Entity> joiningGroup = new ArrayList<>(nearbyEntities.stream()
                .filter(e -> e.getState() == EntityStateType.EXPLORING)
                .toList());
        joiningGroup.add(movedEntity);

        if(existingCombatId != null) {
            log.info("{}'s group joins an existing combat!", movedEntity.getName());
            convertAbilityCooldownByEntityStateType(EntityStateType.COMBAT, joiningGroup);
            activeCombats.get(existingCombatId).addParticipantsToCombat(joiningGroup);
        } else {
            startCombat(joiningGroup);
        }
    }

    private List<Entity> findAllEntitiesInRadius(String entityId, int searchingRadius) {
        Entity entity = entities.get(entityId);
        return entities.values().stream()
                .filter(e -> !e.getId().equals(entity.getId()) &&
                        entity.getPosition().distanceTo(e.getPosition()) <= searchingRadius)
                .toList();
    }

    /**
     * Создает и запускает новый бой с указанными участниками.
     * <p>
     * Метод инициализирует {@link CombatInstance}, переводит способности участников
     * в боевой режим (конвертирует кулдауны), добавляет бой в список активных
     * и оповещает всех клиентов о его начале через событие {@link CombatStartedEvent}.
     *
     * @param participants Все сущности, вступающие в бой.
     */
    public void startCombat(List<Entity> participants) {
        String combatId = UUID.randomUUID().toString();
        CombatInstance combat = new CombatInstance(combatId, participants, aiService, this.getStandartEntityGameSettings().getDefaultEntityCurrentAp(), this);
        combat.start();
        convertAbilityCooldownByEntityStateType(EntityStateType.COMBAT, participants);

        activeCombats.put(combatId, combat);

        publishCombatStartedEvent(combat, combatId);
    }

    // первый аргумент - тип боя, В КОТОРЫЙ нужно перевести КД
    private void convertAbilityCooldownByEntityStateType(EntityStateType entityStateType, List<Entity> participants) {
        for(Entity participant : participants) {
            for (AbilityInstance ability : participant.getAbilities()) {
                if (entityStateType == EntityStateType.COMBAT) {
                    if (ability.getCooldownEndTime() > System.currentTimeMillis()) {
                        long timeLeftMs = ability.getCooldownEndTime() - System.currentTimeMillis();
                        long secondsLeft = timeLeftMs / 1000;
                        int turnsLeft = (int) Math.ceil((double) secondsLeft / 10.0);

                        ability.setTurnCooldown(Math.max(turnsLeft, 0));
                        ability.setCooldownEndTime(0);
                    }
                } else if (ability.getTurnCooldown() > 0) {
                    long secondsToWait = ability.getTurnCooldown() * 10L;
                    long newEndTime = System.currentTimeMillis() + secondsToWait * 1000;
                    ability.setCooldownEndTime(Math.max(newEndTime, 0));
                    ability.setTurnCooldown(0);
                }
            }
            if (participant instanceof Player) {
                publishCasterStateUpdate(participant);
            }
        }
    }
    /**
     * Отправляет клиентам актуальную информацию о текущих очках действия (ОД)
     * и кулдаунах способностей указанного игрока.
     * <p>
     * Вызывается после действий, влияющих на эти параметры (например, каст способности),
     * для поддержания UI на стороне клиента в синхронизированном состоянии.
     *
     * @param player Сущность, чьи данные по ОД и кулдаунам нужно отправить.
     */
    public void publishCasterStateUpdate(Entity player) {
        List<AbilityCooldownDto> cooldowns = player.getAbilities().stream()
                .map(ab -> AbilityCooldownDto.builder()
                        .abilityTemplateId(ab.getTemplate().getTemplateId())
                        .turnCooldown(ab.getTurnCooldown())
                        .cooldownEndTime(ab.getCooldownEndTime())
                        .build())
                .collect(Collectors.toList());

        CasterStateUpdatedEvent updateEvent = CasterStateUpdatedEvent.builder()
                .casterId(player.getId())
                .newCurrentAP(player.getCurrentAP()) // AP не менялись, но поле в DTO обязательное
                .abilityCooldowns(cooldowns)
                .build();

        publishEvent(updateEvent);
    }

    /**
     * Возвращает игрока по entityId
     * @param entityId ID игрока, которого нужно найти
     * @return объект {@link Player} найденный игрок. Или {@code null}, если не нашли
     */
    public Player getPlayerByEntityId(String entityId) {
        Entity entity =  this.entities.get(entityId);
        if(entity instanceof Player)
            return (Player) entity;
        else
            return null;
    }

    private void publishCombatStartedEvent(CombatInstance combat, String combatId) {
        List<CombatTeamDto> combatTeamDtos = combat.getTeams().entrySet().stream()
                .map(entry -> {
                    Set<String> teamMemberIds = entry.getValue().stream()
                            .map(Entity::getId)
                            .collect(Collectors.toSet());
                    return new CombatTeamDto(entry.getKey(), teamMemberIds);
                })
                .toList();

        List<EntityStateDto> combatantDtos = combat.getTurnOrder().stream()
                .map(entityId -> entities.get(entityId))
                .filter(Objects::nonNull)
                .map(entityMapper::toState)
                .collect(Collectors.toList());

        ArrayList<String> turnOrder = new ArrayList<>(combat.getTurnOrder());

        CombatStartedEvent combatStartedEvent = CombatStartedEvent.builder()
                .combatId(combatId)
                .combatInitiatorId(null)
                .teams(combatTeamDtos)
                .initialTurnOrder(turnOrder)
                .combatants(combatantDtos)
                .build();

        publishEvent(combatStartedEvent);
    }

    /**
     * Отправляет стандартизированное сообщение об ошибке конкретному игроку.
     * @param player Игрок, которому отправляется сообщение.
     * @param message Текстовое сообщение для пользователя.
     * @param errorCode Уникальный код ошибки для обработки на клиенте.
     */
    public void sendErrorMessageToPlayer(Player player, String message, String errorCode) {
        notifier.notifyError(player, message, errorCode);
    }

    @Override
    public void onCombatEnded(String combatId, CombatOutcome outcome, String winningTeamId, List<Entity> allParticipants) {
        log.info("Combat {} ended. Outcome: {}", combatId, outcome);

        CombatEndedEvent event = CombatEndedEvent.builder()
                .combatId(combatId)
                .outcome(outcome)
                .winningTeamId(winningTeamId)
                .build();
        publishEvent(event);

        for (Entity participant : allParticipants) {
            if (participant.isAlive()) {
                participant.setState(EntityStateType.EXPLORING);
            }
        }
        convertAbilityCooldownByEntityStateType(EntityStateType.EXPLORING, allParticipants);

        CombatInstance combat = activeCombats.get(combatId);
        if (combat != null) {
            long remainingTeams = combat.getTeams().values().stream()
                    .filter(team -> team.stream().anyMatch(Entity::isAlive))
                    .count();

            if (remainingTeams <= 1) {
                scheduler.schedule(() -> activeCombats.remove(combatId), 1, TimeUnit.SECONDS);
            }
        }
    }

    /**
     * Обработчик события отправки приглашения в команду от одного игрока к другому
     * @param inviterUser игрок, пригласивший в команду.
     * @param targetUser игрок, которого пригласили в команду.
     */
    public void handleInvitationPlayerToTeam(Player inviterUser, Player targetUser) {
        if(inviterUser.equals(targetUser) && Objects.equals(inviterUser.getTeamId(), targetUser.getTeamId())) {
            sendErrorMessageToPlayer(inviterUser, "Cannot invite this player.", "INVITE_INVALID");
            return;
        }
        if(inviterUser.getTeamId() == null) {
            inviterUser.setTeamId(UUID.randomUUID().toString());
            publishTeamUpdated(inviterUser.getTeamId());
        }

        this.pendingInvites.put(targetUser.getUserId(), inviterUser.getTeamId());

        TeamInviteEvent event = new TeamInviteEvent(inviterUser.getId(), inviterUser.getName(), inviterUser.getTeamId());
        notifier.notifyPlayer(targetUser, "team_invite", event);
    }

    /**
     * Собирает актуальный состав команды по её ID и инициирует рассылку
     * события {@link TeamUpdatedEvent} для обновления состояния на клиентах.
     *
     * @param teamId ID команды, которую нужно обновить.
     */
    public void publishTeamUpdated(String teamId) {
        Set<String> membersIds = this.entities.values().stream()
                .filter(e -> Objects.equals(e.getTeamId(), teamId))
                .map(Entity::getId)
                .collect(Collectors.toSet());

        TeamUpdatedEvent event = new TeamUpdatedEvent(teamId, membersIds);
        publishEvent(event);
    }

    /**
     * Обрабатывает ответ игрока на приглашение в команду.
     * <p>
     * Если приглашение принято, игрок добавляется в соответствующую команду, и
     * публикуются обновления для старой и новой команд. Если отклонено,
     * приглашение просто удаляется.
     * <p>
     * Если у игрока не было активных приглашений, ему отправляется ошибка.
     *
     * @param invitedUser игрок, отвечающий на приглашение.
     * @param accepted    {@code true} при согласии, {@code false} при отказе.
     */
    public void handleRespondPlayerToTeamInvite(Player invitedUser, boolean accepted) {
        String teamIdToJoin = pendingInvites.get(invitedUser.getUserId());

        if (teamIdToJoin == null) {
            sendErrorMessageToPlayer(invitedUser, "You have no pending invitation.", "NO_INVITE_FOUND");
            return;
        }

        pendingInvites.remove(invitedUser.getId());

        if (accepted) {
            log.info("Player {} accepted invite to team {}", invitedUser.getName(), teamIdToJoin);

            String oldTeamId = invitedUser.getTeamId();
            if (oldTeamId != null && !oldTeamId.equals(teamIdToJoin)) {
                log.info("Player {} is leaving old team {}", invitedUser.getName(), oldTeamId);
                publishTeamUpdated(oldTeamId);
            }
            invitedUser.setTeamId(teamIdToJoin);

            publishTeamUpdated(teamIdToJoin);

        } else {
            log.info("Player {} declined invite to team {}", invitedUser.getName(), teamIdToJoin);
            // мб найти пригласившего и сообщить об отказе.
        }
    }

    /**
     * Получает игрока по значению userId
     * @param invitedUserId идентификатор пользователя
     * @return объект {@link Player}, который имеет этот userId
     */
    public Player getPlayerByUserId(String invitedUserId) {
        return entities.values().stream()
                .filter(e -> (e instanceof Player) &&
                             ((Player) e).getUserId().equals(invitedUserId))
                .map(e -> (Player) e)
                .findFirst()
                .orElse(null);
    }

    /**
     * Получает сущность, находящуюся на указанном гексе.
     *
     * @param centerHex координата для поиска.
     * @return объект {@link Entity} на этой координате или {@code null}, если она пуста.
     */
    public Entity getEntityAt(Hex centerHex) {
        return entities.values().stream()
                .filter(e -> e.getPosition().equals(centerHex))
                .findFirst()
                .orElse(null);
    }

    /**
     * Обработчик предложения заключить мир.
     * @param combatId идентификатор боя, в котором заключается мир.
     * @param initiatorUserId идентификатор игрока, который предложил заключить мир.
     */
    public void handlePeaceProposal(String combatId, String initiatorUserId) {
        CombatInstance combatInstance = activeCombats.get(combatId);
        if (combatInstance == null) {
            return;
        }
        Player initiator = getPlayerByUserId(initiatorUserId);
        if (initiator == null) return;
        peacefulAgreements.put(combatId, new ConcurrentHashMap<>());
        peacefulAgreements.get(combatId).put(initiatorUserId, true);

        PeaceProposalEvent payload = new PeaceProposalEvent(initiator.getId(), initiator.getName());
        publishUpdateToCombatants(combatInstance, "peace_proposal", payload);

        handleMonsterPresentInPeace(combatId, combatInstance);
    }

    private void handleMonsterPresentInPeace(String combatId, CombatInstance combatInstance) {
        if (combatInstance.getAliveEntities().stream().anyMatch(e -> e instanceof Monster)) {
            log.warn("Peace proposal for combat {} failed because a monster is still alive.", combatId);
            peacefulAgreements.remove(combatId);

            PeaceProposalResultEvent event =
                    new PeaceProposalResultEvent(false,
                                                 "System: Monsters do not agree to peace");
            publishUpdateToCombatants(combatInstance, "peace_proposal_result", event);
        }
    }

    /**
     * Обрабатывает ответ конкретного игрока на активное предложение о мире в рамках боя.
     * <p>
     * Логика разветвляется в зависимости от ответа:
     * <ul>
     *     <li><b>Отказ (accepted = false):</b> Процесс немедленно прерывается. Предложение
     *     о мире считается отклоненным, пул голосов очищается, и все участники боя
     *     получают уведомление об этом.</li>
     *     <li><b>Согласие (accepted = true):</b> Голос игрока записывается. Затем
     *     выполняется проверка, все ли живые игроки в бою теперь проголосовали "за".
     *     Если все "за", то бой завершается мирным путем
     *     ({@link CombatOutcome#END_BY_AGREEMENT}).</li>
     * </ul>
     * Метод ничего не делает, если бой не найден, уже завершен или для него нет
     * активного предложения о мире.
     *
     * @param combatId          ID боя, к которому относится ответ.
     * @param responserUserId   ID пользователя, который отвечает на предложение.
     * @param accepted          {@code true}, если игрок согласен на мир, иначе {@code false}.
     */
    public void handlePeaceResponse(String combatId, String responserUserId, boolean accepted) {
        CombatInstance combatInstance = activeCombats.get(combatId);
        Map<String, Boolean> votes = peacefulAgreements.get(combatId);

        if (combatInstance == null || combatInstance.isFinished() || votes == null)
            return;

        Player responder = getPlayerByUserId(responserUserId);
        if (responder == null || !responder.isAlive())
            return;

        handleMonsterPresentInPeace(combatId, combatInstance);

        if (!accepted) {
            log.info("Peace proposal for combat {} was rejected by {}.", combatId, responder.getName());
            peacefulAgreements.remove(combatId);

            PeaceProposalResultEvent payload = new PeaceProposalResultEvent(false, responder.getName());
            publishUpdateToCombatants(combatInstance, "peace_proposal_result", payload);
            return;
        }

        votes.put(responserUserId, true);

        List<String> alivePlayerUserIds = combatInstance.getAliveEntities().stream()
                .filter(e -> e instanceof Player)
                .map(e -> ((Player) e).getUserId())
                .toList();

        boolean allPlayersVoted = alivePlayerUserIds.stream().allMatch(votes::containsKey);

        if (votes.size() >= alivePlayerUserIds.size() && allPlayersVoted) {
            log.info("Peace proposal for combat {} was unanimously accepted!", combatId);
            peacefulAgreements.remove(combatId);

            PeaceProposalResultEvent payload = new PeaceProposalResultEvent(true, null);
            publishUpdateToCombatants(combatInstance, "peace_proposal_result", payload);

            combatInstance.endCombatForAllByAgreement();
        }
    }

    private void publishUpdateToCombatants(CombatInstance combat, String updateType, Object payload) {
        List<Player> playersInCombat = combat.getAliveEntities().stream()
                .filter(e -> e instanceof Player)
                .map(e -> (Player) e)
                .toList();
        notifier.notifyPlayers(playersInCombat, updateType, payload);
    }

    /**
     * Обработчик выхода игрока из команды
     * @param userId идентификатор игрока
     */
    public void handleLeaveFromTeam(String userId) {
        Player playerWhoLeaves = getPlayerByUserId(userId);
        if(playerWhoLeaves == null)
            return;

        String oldTeamId = playerWhoLeaves.getTeamId();

        String newSoloTeamId = UUID.randomUUID().toString();
        playerWhoLeaves.setTeamId(newSoloTeamId);

        publishTeamUpdated(oldTeamId);
        publishTeamUpdated(playerWhoLeaves.getTeamId());
    }

    /**
     * Обработчик отключения игрока от сессии.
     * @param websocketSessionId ID отключенной WebSocket-сессии.
     */
    public void handlePlayerDisconnect(String websocketSessionId) {
        Player disconnectedPlayer = getPlayerByWebsocketSessionId(websocketSessionId);
        if (disconnectedPlayer == null) {
            return;
        }
        endTurn(disconnectedPlayer.getId());

        this.removeEntity(disconnectedPlayer);
        PlayerLeftEvent event = new PlayerLeftEvent(disconnectedPlayer.getId());
        publishEvent(event);

        if (!disconnectedPlayer.getTeamId().equals(disconnectedPlayer.getId())) {
            publishTeamUpdated(disconnectedPlayer.getTeamId());
        }

        if (!hasHumanPlayers()) {
            log.info("Last player disconnected from session {}. Ending session.", this.sessionID);
            notifyGameSessionEndListeners();
        }
    }

    private boolean hasHumanPlayers() {
        return this.entities.values().stream()
                .anyMatch(entity -> entity instanceof Player);
    }

    @Override
    public Entity getEntityById(String id) {
        return this.entities.get(id);
    }

    @Override
    public void scheduleAiTurn(String monsterId) {
        this.scheduler.schedule(() -> aiService.executeMonsterTurn(monsterId, this), 1, TimeUnit.SECONDS);
    }

    @Override
    public void onCombatEvent(Object event) {
        this.publishEvent(event);
    }
}
