package dev.mygame.config;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import java.security.Principal;
import java.util.Map;
import java.util.UUID;

public class CustomHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // Генерируем уникальный ID для каждого нового подключения и используем его как имя Principal
        return new WebSocketPrincipal(UUID.randomUUID().toString());
        // Позже, когда будет аутентификация, здесь можно будет извлекать имя пользователя из HTTP-сессии или токена
    }
}
