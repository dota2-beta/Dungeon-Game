package com.abs.dungeoncrawler.gamesessionservice.services;

import com.abs.dungeoncrawler.gamesessionservice.domain.GameSession;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionRepository {
    private final Map<String, GameSession> activeSessions = new ConcurrentHashMap<>();

    public void save(GameSession session) {
        activeSessions.put(session.getSessionId(), session);
    }

    public Optional<GameSession> findById(String sessionId) {
        return Optional.ofNullable(activeSessions.get(sessionId));
    }

    public void delete(String sessionId) {
        activeSessions.remove(sessionId);
    }

}
