package dev.mygame.config;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import java.security.Principal;
import java.util.Map;
import java.util.UUID;

/**
 * Пользовательский обработчик handshake для WebSocket соединений
 * <p>
 * При каждом новом подключении он генерирует случайный UUID и
 * создает для сессии временный {@link WebSocketPrincipal}.
 */
public class CustomHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        return new WebSocketPrincipal(UUID.randomUUID().toString());
    }
}
