package dev.mygame.config;

public final class WebSocketDestinations {
    // --- Общие топики (рассылка всем в сессии) ---
    public static final String GAME_UPDATES_TOPIC = "/topic/session/{sessionId}/game-updates";
    public static final String CHAT_TOPIC = "/topic/session/{sessionId}/chat";
    public static final String TURN_ORDER_TOPIC = "/topic/session/{sessionId}/turn-order";


    // --- Личные топики (рассылка конкретному пользователю) ---
    // Формат для отправки на бэкенде (без /user)
    public static final String SESSION_STATE_QUEUE = "/queue/session-state";
    public static final String ERROR_QUEUE = "/queue/errors";
    public static final String PRIVATE_MESSAGE_QUEUE = "/queue/private";


    // --- Адреса для сообщений от клиента к серверу ---
    public static final String CREATE_SESSION_DEST = "/app/create-session";
    public static final String JOIN_SESSION_DEST = "/app/join-session";
    public static final String PLAYER_ACTION_DEST = "/app/session/{sessionId}/action";


    // Приватный конструктор, чтобы нельзя было создать экземпляр
    private WebSocketDestinations() {}
}
