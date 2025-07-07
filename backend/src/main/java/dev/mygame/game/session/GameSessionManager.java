package dev.mygame.game.session;

import dev.mygame.config.GameSettings;
import dev.mygame.config.WebSocketDestinations;
import dev.mygame.dto.GameParametersRequest;
import dev.mygame.dto.PlayerAction;
import dev.mygame.game.enums.EntityState;
import dev.mygame.game.model.Entity;
import dev.mygame.game.model.GameObject;
import dev.mygame.game.model.Player;
import dev.mygame.game.model.map.GameMap;
import dev.mygame.game.model.map.Point;
import dev.mygame.game.session.event.GameSessionEndListener;
import dev.mygame.mapper.EntityActionMapper;
import dev.mygame.mapper.EntityMapper;
import dev.mygame.mapper.GameSessionMapper;
import jakarta.annotation.PostConstruct;
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
    private final MapGenerator mapGenerator;
    private final GameDataLoader gameDataLoader

    private final GameSessionMapper gameSessionMapper;
    private final EntityMapper entityMapper;
    private final EntityActionMapper entityActionMapper;

    @Autowired
    public GameSessionManager(SimpMessagingTemplate messagingTemplate, GameSettings gameSettings,
                              MapGenerator mapGenerator, GameDataLoader gameDataLoader, GameSessionMapper gameSessionMapper, EntityMapper entityMapper, EntityActionMapper entityActionMapper) {
        this.gameSessionMapper = gameSessionMapper;
        this.entityMapper = entityMapper;
        this.entityActionMapper = entityActionMapper;
        this.activeSessions = new ConcurrentHashMap<>();
        this.messagingTemplate = messagingTemplate;
        this.gameSettings = gameSettings;
        this.mapGenerator = mapGenerator;
        this.gameDataLoader = gameDataLoader;
    }

    @PostConstruct
    public void createTestSessionOnStartup() {
        System.out.println(">>> Creating a test game session on application startup...");
        String testSessionId = createSession();
        System.out.println("=================================================");
        System.out.println(">>> TEST SESSION CREATED. ID: " + testSessionId);
        System.out.println(">>> Use this ID on the frontend to join the game.");
        System.out.println("=================================================");
    }

    public String createSession() {
        String sessionId = UUID.randomUUID().toString();
        GameMap gameMap = mapGenerator.generateMap();

        Map<String, Entity> initialEntities = new ConcurrentHashMap<>();
        Map<String, GameObject> initialGameObjects = new ConcurrentHashMap<>();

        GameSession gameSession = GameSession.builder()
                .sessionID(sessionId)
                .gameMap(gameMap)
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

        Point startPosition = new Point(gameSession.getGameMap().getWidth() / 2, gameSession.getGameMap().getHeight() / 2);

        int baseMaxHp = 100;
        int baseAttack = 10;
        int baseDefense = 5;
        int baseInitiative = 10;
        int baseMaxAp = 6;
        int baseAttackRange = 1;
        EntityState initialState = EntityState.EXPLORING;
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

        messagingTemplate.convertAndSendToUser(
                userId,
                WebSocketDestinations.SESSION_STATE_QUEUE.replace("{sessionId}", gameSession.getSessionID()),
                gameSessionMapper.toGameSessionState(gameSession, userId)
        );

        gameSession.publishUpdate("player_joined", entityMapper.toPlayerClientState(player));
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
