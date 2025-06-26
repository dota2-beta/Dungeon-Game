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

    private final SimpMessagingTemplate messagingTemplate; // Для передачи в GameSession
    private final GameSettings gameSettings; // Общие настройки
    private final MapGenerator mapGenerator; // **Вот он!**
    private final GameDataLoader gameDataLoader; // Нужен для загрузки DungeonTemplate

    private final GameSessionMapper gameSessionMapper;
    private final EntityMapper entityMapper;
    private final EntityActionMapper entityActionMapper;

    @Autowired
    public GameSessionManager(SimpMessagingTemplate messagingTemplate, GameSettings gameSettings,
                              MapGenerator mapGenerator, GameDataLoader gameDataLoader, GameSessionMapper gameSessionMapper, EntityMapper entityMapper, EntityActionMapper entityActionMapper) {
        this.gameSessionMapper = gameSessionMapper;
        this.entityMapper = entityMapper;
        this.entityActionMapper = entityActionMapper;
        this.activeSessions = new ConcurrentHashMap<>(); // Используем ConcurrentHashMap
        this.messagingTemplate = messagingTemplate;
        this.gameSettings = gameSettings;
        this.mapGenerator = mapGenerator; // Сохраняем внедренный MapGenerator
        this.gameDataLoader = gameDataLoader; // Сохраняем внедренный GameDataLoader
    }

    @PostConstruct
    public void createTestSessionOnStartup() {
        System.out.println(">>> Creating a test game session on application startup...");
        // Вызываем ваш метод создания сессии с пустыми параметрами
        String testSessionId = createSession(); // Используем упрощенный метод
        System.out.println("=================================================");
        System.out.println(">>> TEST SESSION CREATED. ID: " + testSessionId);
        System.out.println(">>> Use this ID on the frontend to join the game.");
        System.out.println("=================================================");
    }

    //public String createSession(GameParametersRequest gameParametersRequest) {
    public String createSession() {
        String sessionId = UUID.randomUUID().toString();
        GameMap gameMap = mapGenerator.generateMap();

        Map<String, Entity> initialEntities = new ConcurrentHashMap<>();
        Map<String, GameObject> initialGameObjects = new ConcurrentHashMap<>();

        GameSession gameSession = GameSession.builder()
                .sessionID(sessionId)
                .gameMap(gameMap) // Передаем сгенерированную карту
                .gameSettings(this.gameSettings) // Передаем общие настройки
                .messagingTemplate(this.messagingTemplate) // Передаем messagingTemplate
                .entities(initialEntities) // TODO: Убедитесь, что GameSession Builder принимает Map<String, Entity> entities
                .gameObjects(initialGameObjects) // TODO: Убедитесь, что GameSession Builder принимает Map<String, GameObject> gameObjects
                // TODO: Другие поля GameSession, если нужны (например, список начальных игроков как Player объектов, а не просто Entity)
                .build(); // Завершаем сборку объекта
        gameSession.addGameSessionEndListener(this);

        // 5. TODO: Добавить игроков (Player сущностей) в GameSession, если они еще не добавлены в initialEntities
        // Например, если Player объекты создаются здесь по userIds из запроса
        // gameSession.addPlayer(userId, ...); // Метод в GameSession

        // 6. Добавить новую сессию в Map активных сессий
        activeSessions.put(sessionId, gameSession);

        // 7. TODO: Возможно, выполнить дополнительные действия после создания сессии
        // Например, разослать сообщение клиентам о том, что сессия создана и готова к присоединению

        return sessionId; // Возвращаем ID созданной сессии
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
             // Возможно, найти существующего игрока, обновить его websocketSessionId и отправить состояние.
             //throw new IllegalArgumentException("User " + userId + " is already in session " + sessionId);
         }

        String entityId = UUID.randomUUID().toString();

        Point startPosition = new Point(gameSession.getGameMap().getWidth() / 2, gameSession.getGameMap().getHeight() / 2);

        int baseMaxHp = 100;
        int baseAttack = 10;
        int baseDefense = 5;
        int baseInitiative = 10;
        int baseMaxAp = 6;
        EntityState initialState = EntityState.EXPLORING; // Игрок присоединяется в режиме исследования
//        List<DeathListener> deathListeners = new ArrayList<>(); // Начальный список слушателей смерти (будет добавлен CombatInstance)
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
                .state(initialState)
                .userId(userId) // Устанавливаем поля userId и websocketSessionId
                .websocketSessionId(webSocketSessionId)
                .build();
        gameSession.addEntity(player);

        messagingTemplate.convertAndSendToUser(
                webSocketSessionId,
                WebSocketDestinations.SESSION_STATE_QUEUE,
                gameSessionMapper.toGameSessionState(gameSession, userId)
        );

        gameSession.publishUpdate("PLAYER_JOINED", entityMapper.toPlayerClientState(player));
    }

    public void handlePlayerAction(String sessionId, String websocketSessionId, PlayerAction playerAction) {
        GameSession gameSession = activeSessions.get(sessionId);
        if (gameSession == null) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }
        Player actingPlayer = gameSession.getPlayerByWebsocketSessionId(websocketSessionId);

        gameSession.handleEntityAction(actingPlayer.getId(), entityActionMapper.toEntityAction(playerAction));
    }

    @Override
    public void onGameSessionEnd(GameSession session) {
        boolean removed = this.activeSessions.remove(session.getSessionID(), session); // Удаляем сессию по ID и проверяем, что удалили нужный объект
        if (removed) {
            System.out.println("Game Session " + session.getSessionID() + " ended with outcome: ");
            // TODO: Возможно, разослать сообщение всем клиентам, которые были в этой сессии, о ее завершении
            // TODO: Очистить ресурсы, связанные с сессией, если есть
        } else {
            // TODO: Логировать ошибку - сессия не найдена в activeSessions при попытке завершения
        }
    }
}
