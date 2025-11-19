package com.abs.dungeoncrawler.websocketservice;

import com.abs.dungeoncrawler.websocketservice.clients.GameSessionClient;
import com.abs.dungeoncrawler.websocketservice.dto.JoinRequestDto;
import com.abs.dungeoncrawler.websocketservice.registry.ConnectionRegistry;
import com.dungeoncrawler.contracts.grpc.gamesession.JoinRequest;
import com.dungeoncrawler.contracts.grpc.gamesession.JoinResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
@RequiredArgsConstructor
@Slf4j
public class ActionDispatcher {
    private final GameSessionClient gameSessionClient;
    private final ConnectionRegistry  connectionRegistry;

    public void dispatchJoinSession(
            JoinRequestDto joinRequestDto,
            Principal principal,
            String webSocketSessionId) {
        String username = joinRequestDto.getUsername();
        String sessionId = joinRequestDto.getSessionId();
        String userId = principal.getName();
        log.info("Dispatching join request for user {} to session {}", username, sessionId);

        JoinRequest grpcRequest = JoinRequest.newBuilder()
                .setSessionId(sessionId)
                .setUsername(username)
                .setTemplateId(joinRequestDto.getTemplateId())
                .setUserId(userId)
                .build();
        try {
            JoinResponse response = gameSessionClient.joinSession(grpcRequest);

            if(response.getSuccess()) {
                log.info("User {} successfully connected to session {}", username, sessionId);
                connectionRegistry.register(webSocketSessionId, userId, sessionId);
            } else {
                log.error("User {} couldn't connect to session {}", username, sessionId);
            }
        } catch (RuntimeException e) {
            log.error("The call to GameSessionService failed. User will not be connected", e);
        }
    }
}
