package dev.mygame.domain.event;

import dev.mygame.service.GameSessionManager;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
public class SessionDisconnectListener implements ApplicationListener<SessionDisconnectEvent> {

    private static final Logger log = LoggerFactory.getLogger(SessionDisconnectListener.class);
    private final GameSessionManager gameSessionManager;

    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {
        String websocketSessionId = event.getSessionId();
        if (websocketSessionId == null) {
            return;
        }

        log.info("WebSocket session disconnected: {}", websocketSessionId);
        gameSessionManager.handlePlayerDisconnect(websocketSessionId);
    }
}
