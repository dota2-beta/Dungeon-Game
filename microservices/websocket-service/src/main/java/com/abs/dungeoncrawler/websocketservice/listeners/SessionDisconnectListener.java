package com.abs.dungeoncrawler.websocketservice.listeners;

import com.abs.dungeoncrawler.websocketservice.registry.ConnectionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class SessionDisconnectListener implements ApplicationListener<SessionDisconnectEvent> {
    private final ConnectionRegistry connectionRegistry;

    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {
        String websocketSessionId = event.getSessionId();
        connectionRegistry.remove(event.getSessionId());
        log.info("User with websocket session id {} disconnected", websocketSessionId);
    }
}
