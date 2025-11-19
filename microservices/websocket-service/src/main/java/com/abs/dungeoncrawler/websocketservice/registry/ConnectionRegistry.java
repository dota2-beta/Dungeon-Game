package com.abs.dungeoncrawler.websocketservice.registry;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ConnectionRegistry {
    private record UserSessionInfo(String userId, String gameSessionId) { }
    private ConcurrentHashMap<String, UserSessionInfo> registry = new ConcurrentHashMap<>();

    public void register(String webSocketSessionId, String userId, String gameSessionId) {
        registry.put(webSocketSessionId, new UserSessionInfo(userId, gameSessionId));
    }

    public void remove(String webSocketSessionId) {
        registry.remove(webSocketSessionId);
    }

    public Optional<String> getUserId(String websocketSessionId) {
        return  Optional.ofNullable(registry.get(websocketSessionId))
                    .map(UserSessionInfo::userId);
    }

    public Set<String> findWebSocketSessionIdsByGameSessionId(String gameSessionId) {
        return registry.entrySet().stream()
                .filter(x -> gameSessionId.equals(x.getValue().gameSessionId()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
   }
}
