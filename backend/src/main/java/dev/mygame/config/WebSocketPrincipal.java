package dev.mygame.config;

import java.security.Principal;

/**
 * Класс, реализующий интерфейс {@link Principal}, который
 * будет использоваться при установке WebSocket соединения
 */
public class WebSocketPrincipal implements Principal {
    private String name;
    public WebSocketPrincipal(String name) { this.name = name; }
    @Override
    public String getName() { return name; }
}
