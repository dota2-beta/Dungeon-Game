package dev.mygame.service;

import dev.mygame.config.StandartEntityGameSettings;
import dev.mygame.config.MapGenerationProperties;
import dev.mygame.config.WebSocketDestinations;
import dev.mygame.data.MapLoader;
import dev.mygame.domain.factory.EntityFactory;
import dev.mygame.domain.model.map.GameMapHex;
import dev.mygame.domain.model.map.Hex;
import dev.mygame.dto.websocket.request.JoinRequest;
import dev.mygame.dto.websocket.request.PlayerAction;
import dev.mygame.domain.model.GameObject;
import dev.mygame.domain.model.Player;
import dev.mygame.domain.event.GameSessionEndListener;
import dev.mygame.data.GameDataLoader;
import dev.mygame.domain.session.GameSession;
import dev.mygame.mapper.EntityActionMapper;
import dev.mygame.mapper.EntityMapper;
import dev.mygame.mapper.GameSessionMapper;
import dev.mygame.mapper.context.MappingContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

@Data
@AllArgsConstructor
@Service
public class GameSessionManager implements GameSessionEndListener {
    public Map<String, GameSession> activeSessions;

    private final SimpMessagingTemplate messagingTemplate;
    private final StandartEntityGameSettings standartEntityGameSettings;
    private final MapGenerationProperties props;
    private final MapGenerator mapGenerator;
    private final MapLoader mapLoader;
    private final GameDataLoader gameDataLoader;

    private final GameSessionMapper gameSessionMapper;
    private final EntityMapper entityMapper;
    private final EntityActionMapper entityActionMapper;

    private final FactionService factionService;
    private final AbilityService abilityService;
    private final MonsterSpawnerService spawnerService;
    private final AIService aiService;
    private final EntityFactory entityFactory;

    private static final Logger log = LoggerFactory.getLogger(GameSessionManager.class);
    private final ScheduledExecutorService scheduler;

    @Autowired
    public GameSessionManager(SimpMessagingTemplate messagingTemplate, StandartEntityGameSettings standartEntityGameSettings, MapGenerationProperties props,
                              MapGenerator mapGenerator, MapLoader mapLoader, GameDataLoader gameDataLoader, GameSessionMapper gameSessionMapper, EntityMapper entityMapper, EntityActionMapper entityActionMapper, FactionService factionService, AbilityService abilityService, MonsterSpawnerService spawnerService, AIService aiService, EntityFactory entityFactory, ScheduledExecutorService scheduler) {
        this.props = props;
        this.mapLoader = mapLoader;
        this.gameSessionMapper = gameSessionMapper;
        this.entityMapper = entityMapper;
        this.entityActionMapper = entityActionMapper;
        this.factionService = factionService;
        this.abilityService = abilityService;
        this.spawnerService = spawnerService;
        this.aiService = aiService;
        this.entityFactory = entityFactory;
        this.scheduler = scheduler;
        this.activeSessions = new ConcurrentHashMap<>();
        this.messagingTemplate = messagingTemplate;
        this.standartEntityGameSettings = standartEntityGameSettings;
        this.mapGenerator = mapGenerator;
        this.gameDataLoader = gameDataLoader;
    }

    public String createSession() {
        String sessionId = UUID.randomUUID().toString();
        //GameMapHex gameMapHex = mapGenerator.generateHexBattleArena(props);
        GameMapHex gameMapHex;
        try {
            gameMapHex = mapLoader.loadMapFromFile("gamedata/maps/dungeon_level_1.txt");
        } catch (Exception e) {
            log.error("Failed to load map file!", e);
            //gameMapHex = mapGenerator.generateHexBattleArena(props);
            throw new RuntimeException("Could not create game session, map failed to load.", e);
        }

        Map<String, GameObject> initialGameObjects = new ConcurrentHashMap<>();

        GameSession gameSession = GameSession.builder()
                .sessionID(sessionId)
                .scheduler(scheduler)
                .gameMap(gameMapHex)
                .standartEntityGameSettings(this.standartEntityGameSettings)
                .messagingTemplate(this.messagingTemplate)
                .gameObjects(initialGameObjects)
                .factionService(factionService)
                .entityMapper(entityMapper)
                .abilityService(abilityService)
                .aiService(aiService)
                .build();
        gameSession.addGameSessionEndListener(this);
        spawnerService.spawnMonstersForMap(gameSession);
        // TODO: Добавить игроков (Player сущностей) в GameSession, если они еще не добавлены в initialEntities
        // gameSession.addPlayer(userId, ...); // Метод в GameSession

        activeSessions.put(sessionId, gameSession);

        // TODO: Возможно, выполнить дополнительные действия после создания сессии

        return sessionId;
    }

    public void joinPlayer(JoinRequest request, String userId, String sessionId, String websocketSessionId) {
        GameSession gameSession = activeSessions.get(sessionId);

        if (gameSession == null) {
            throw new IllegalArgumentException("Game Session with ID " + sessionId + " not found.");
        }
        boolean userAlreadyInSession = gameSession.getEntities().values().stream()
                .filter(en -> en instanceof Player)
                .map(en -> (Player) en)
                .anyMatch(player -> player.getUserId().equals(userId));

         if (userAlreadyInSession) {
             // TODO: Обработать случай переподключения или ошибки (пользователь уже есть)
         }
        GameMapHex gameMap = gameSession.getGameMap();

        String playerClassId = request.getTemplateId();
        Hex startPosition = gameMap.getAvailablePlayerSpawnPoint();

        Player player = entityFactory.createPlayer(playerClassId, userId,request.getName(), websocketSessionId, startPosition);

        gameSession.addEntity(player);

        sendInitialStateToPlayer(gameSession, userId);
        gameSession.publishUpdate("player_joined", entityMapper.toPlayerState(player));

        gameSession.checkForCombatStart(player);
    }

    public void handlePlayerAction(String sessionId, String websocketSessionId, PlayerAction playerAction) {
        GameSession gameSession = activeSessions.get(sessionId);
        if (gameSession == null)
            throw new IllegalArgumentException("Session not found: " + sessionId);

        Player actingPlayer = gameSession.getPlayerByWebsocketSessionId(websocketSessionId);

        if(actingPlayer == null)
            throw new IllegalArgumentException("Player not found.");

        gameSession.handleEntityAction(actingPlayer.getId(), entityActionMapper.toEntityAction(playerAction));
    }

    public void sendInitialStateToPlayer(GameSession gameSession, String userId) {
        MappingContext context = new MappingContext(
                userId,
                props.getBattleArenaRadius()
        );

        messagingTemplate.convertAndSendToUser(
                userId,
                WebSocketDestinations.SESSION_STATE_QUEUE.replace("{sessionId}", gameSession.getSessionID()),
                gameSessionMapper.toGameSessionState(gameSession, context)
        );
    }

    public void inviteToTeam(String sessionId, String inviterUserId, String invitedUserId) {
        activeSessions.get(sessionId);
    }

    @Override
    public void onGameSessionEnd(GameSession session) {
        boolean removed = this.activeSessions.remove(session.getSessionID(), session);
        if (removed) {
            // TODO: Возможно, разослать сообщение всем клиентам, которые были в этой сессии, о ее завершении
            // TODO: Очистить ресурсы, связанные с сессией, если есть
        } else {
            // TODO: Логировать ошибку - сессия не найдена в activeSessions при попытке завершения
        }
    }

    public void invitePlayerToTeam(String sessionId, String inviterUserId, String targetPlayerId) {
        GameSession session = activeSessions.get(sessionId);
        if(session == null)
            return;

        Player inviterUser = session.getPlayerByUserId(inviterUserId);
        Player targetUser = session.getPlayerByEntityId(targetPlayerId);

        if(inviterUser != null && targetUser != null)
            session.handleInvitationPlayerToTeam(inviterUser, targetUser);
    }

    public void respondPlayerToTeamInvite(String sessionId, String invitedUserId, boolean accepted) {
        GameSession session = activeSessions.get(sessionId);
        if(session == null)
            return;

        Player invitedUser = session.getPlayerByUserId(invitedUserId);

        if(invitedUser != null)
            session.handleRespondPlayerToTeamInvite(invitedUser, accepted);
    }

    public void handlePeaceProposal(String sessionId, String initiatorUserId, String combatId) {
        GameSession session = activeSessions.get(sessionId);
        if(session == null)
            return;
        session.handlePeaceProposal(combatId, initiatorUserId);
    }

    public void handlePeaceResponse(String sessionId, String name, String combatId, boolean accepted) {
        GameSession session = activeSessions.get(sessionId);
        if(session == null)
            return;
        session.handlePeaceResponse(combatId, name, accepted);
    }

    public void leaveFromTeam(String sessionId, String userId) {
        GameSession session = activeSessions.get(sessionId);
        if(session == null)
            return;
        session.handleLeaveFromTeam(userId);
    }

    public void resendStateToPlayer(String sessionId, String userId) {
        GameSession gameSession = activeSessions.get(sessionId);
        if (gameSession == null) {
            log.warn("Attempted to resend state for non-existent session: {}", sessionId);
            return;
        }

        sendInitialStateToPlayer(gameSession, userId);
    }
}
