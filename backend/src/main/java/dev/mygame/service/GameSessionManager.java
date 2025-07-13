package dev.mygame.service;

import dev.mygame.config.GameSettings;
import dev.mygame.config.MapGenerationProperties;
import dev.mygame.config.WebSocketDestinations;
import dev.mygame.domain.model.map.GameMapHex;
import dev.mygame.domain.model.map.Hex;
import dev.mygame.dto.websocket.request.PlayerAction;
import dev.mygame.dto.websocket.response.GameSessionStateDto;
import dev.mygame.enums.EntityStateType;
import dev.mygame.domain.model.Entity;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Data
@AllArgsConstructor
@Service
public class GameSessionManager implements GameSessionEndListener {
    public Map<String, GameSession> activeSessions;

    private final SimpMessagingTemplate messagingTemplate;
    private final GameSettings gameSettings;
    private final MapGenerationProperties props;
    private final MapGenerator mapGenerator;
    private final GameDataLoader gameDataLoader;

    private final GameSessionMapper gameSessionMapper;
    private final EntityMapper entityMapper;
    private final EntityActionMapper entityActionMapper;

    @Autowired
    public GameSessionManager(SimpMessagingTemplate messagingTemplate, GameSettings gameSettings, MapGenerationProperties props,
                              MapGenerator mapGenerator, GameDataLoader gameDataLoader, GameSessionMapper gameSessionMapper, EntityMapper entityMapper, EntityActionMapper entityActionMapper) {
        this.props = props;
        this.gameSessionMapper = gameSessionMapper;
        this.entityMapper = entityMapper;
        this.entityActionMapper = entityActionMapper;
        this.activeSessions = new ConcurrentHashMap<>();
        this.messagingTemplate = messagingTemplate;
        this.gameSettings = gameSettings;
        this.mapGenerator = mapGenerator;
        this.gameDataLoader = gameDataLoader;
    }

    public String createSession() {
        String sessionId = UUID.randomUUID().toString();
        //GameMap gameMap = mapGenerator.generateDungeon();
        GameMapHex gameMapHex = mapGenerator.generateHexBattleArena(props);

        Map<String, Entity> initialEntities = new ConcurrentHashMap<>();
        Map<String, GameObject> initialGameObjects = new ConcurrentHashMap<>();

        GameSession gameSession = GameSession.builder()
                .sessionID(sessionId)
                .gameMap(gameMapHex)
                .gameSettings(this.gameSettings)
                .messagingTemplate(this.messagingTemplate)
                .entities(initialEntities)
                .gameObjects(initialGameObjects)
                .build();
        gameSession.addGameSessionEndListener(this);

        // TODO: Добавить игроков (Player сущностей) в GameSession, если они еще не добавлены в initialEntities
        // gameSession.addPlayer(userId, ...); // Метод в GameSession

        // Добавить новую сессию в Map активных сессий
        activeSessions.put(sessionId, gameSession);

        // TODO: Возможно, выполнить дополнительные действия после создания сессии

        return sessionId;
    }

    public void joinPlayer(String userId, String sessionId, String webSocketSessionId) {
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
             // найти существующего игрока, обновить его websocketSessionId и отправить состояние.
             //throw new IllegalArgumentException("User " + userId + " is already in session " + sessionId);
         }

        String entityId = UUID.randomUUID().toString();

        Hex startPosition = new Hex(0, 0);

        //Point startPosition = gameSession.getGameMap().getSpawnPoint();

        int baseMaxHp = 100;
        int baseAttack = 10;
        int baseDefense = 5;
        int baseInitiative = 10;
        int baseMaxAp = 6;
        int baseAttackRange = 1;
        EntityStateType initialState = EntityStateType.EXPLORING;
//        List<DeathListener> deathListeners = new ArrayList<>();
//        List<Item> initialInventory = new ArrayList<>();

        Player player = Player.builder()
                .id(entityId)
                .position(startPosition)
                .maxHp(baseMaxHp)
                .currentHp(baseMaxHp)
                .attack(baseAttack)
                .defense(baseDefense)
                .initiative(baseInitiative)
                .maxAP(baseMaxAp)
                .currentAP(baseMaxAp)
                .attackRange(baseAttackRange)
                .state(initialState)
                .userId(userId)
                .websocketSessionId(webSocketSessionId)
                .build();
        gameSession.addEntity(player);

        sendInitialStateToPlayer(gameSession, userId);
        gameSession.publishUpdate("player_joined", entityMapper.toPlayerState(player));
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

        GameSessionStateDto stateDto = gameSessionMapper.toGameSessionState(gameSession, context);

        messagingTemplate.convertAndSendToUser(
                userId,
                WebSocketDestinations.SESSION_STATE_QUEUE.replace("{sessionId}", gameSession.getSessionID()),
                gameSessionMapper.toGameSessionState(gameSession, context)
        );
    }

    @Override
    public void onGameSessionEnd(GameSession session) {
        boolean removed = this.activeSessions.remove(session.getSessionID(), session);
        if (removed) {
            System.out.println("Game Session " + session.getSessionID() + " ended with outcome: ");
            // TODO: Возможно, разослать сообщение всем клиентам, которые были в этой сессии, о ее завершении
            // TODO: Очистить ресурсы, связанные с сессией, если есть
        } else {
            // TODO: Логировать ошибку - сессия не найдена в activeSessions при попытке завершения
        }
    }
}
