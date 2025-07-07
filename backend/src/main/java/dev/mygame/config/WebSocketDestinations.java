package dev.mygame.config;

public final class WebSocketDestinations {
    // --- Общие топики (рассылка всем в сессии) ---
    public static final String GAME_UPDATES_TOPIC = "/topic/session/{sessionId}/game-updates";
    public static final String CHAT_TOPIC = "/topic/session/{sessionId}/chat";
    public static final String TURN_ORDER_TOPIC = "/topic/session/{sessionId}/turn-order";


    // --- Личные топики (рассылка конкретному пользователю) ---
    public static final String SESSION_STATE_QUEUE = "/queue/session/{sessionId}/state";
    public static final String ERROR_QUEUE = "/queue/errors";
    public static final String PRIVATE_NOTIFICATION_MESSAGE_QUEUE = "/queue/notification/private";


    // --- Адреса для сообщений от клиента к серверу ---
    public static final String CREATE_SESSION_DEST = "/app/create-session";
    public static final String JOIN_SESSION_DEST = "/app/join-session";
    public static final String PLAYER_ACTION_DEST = "/app/session/{sessionId}/action";

    public static final String SESSION_CREATED_TOPIC = "/topic/session-created-response";

    private WebSocketDestinations() {}
}
