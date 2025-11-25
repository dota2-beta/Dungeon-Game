package com.abs.dungeoncrawler.websocketservice;

import com.abs.dungeoncrawler.websocketservice.clients.GameSessionClient;
import com.abs.dungeoncrawler.websocketservice.dto.JoinRequestDto;
import com.abs.dungeoncrawler.websocketservice.registry.ConnectionRegistry;
import com.dungeoncrawler.contracts.grpc.gamesession.JoinRequest;
import com.dungeoncrawler.contracts.grpc.gamesession.JoinResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class ActionDispatcherTest {
    @Mock
    private GameSessionClient gameSessionClient;
    @Mock
    private ConnectionRegistry connectionRegistry;
    @InjectMocks
    private ActionDispatcher dispatcher;

    @Test
    void whenDispatchJoinSession_ThenGrpcClientIsCalledWithCorrectData() {
        //arrange
        String sessionId = "testSessionId";
        String wsSessionId = "testWsSessionId";
        String userId = "testUserId";
        JoinRequestDto joinRequestDto = JoinRequestDto.builder()
                .sessionId(sessionId)
                .templateId("testTemplateId")
                .username("testUsername")
                .build();
        Principal principal = Mockito.mock(Principal.class);

        JoinResponse successfulResponse = JoinResponse.newBuilder()
                .setSuccess(true)
                .build();

        Mockito.when(principal.getName()).thenReturn(userId);
        Mockito.when(gameSessionClient.joinSession(any())).thenReturn(successfulResponse);
        //act
        dispatcher.dispatchJoinSession(joinRequestDto, principal, wsSessionId);
        //assert
        ArgumentCaptor<JoinRequest> captor = ArgumentCaptor.forClass(JoinRequest.class);
        Mockito.verify(gameSessionClient, Mockito.times(1)).joinSession(captor.capture());
        Mockito.verify(connectionRegistry, Mockito.times(1)).register(wsSessionId, userId, sessionId);

        JoinRequest grpcRequest = captor.getValue();
        assertEquals(joinRequestDto.getSessionId(), grpcRequest.getSessionId());
        assertEquals(joinRequestDto.getTemplateId(), grpcRequest.getTemplateId());
        assertEquals(joinRequestDto.getUsername(), grpcRequest.getUsername());
        assertEquals(principal.getName(), grpcRequest.getUserId());
    }

    @Test
    void shouldNotRegisterUser_whenGrpcReturnsFailure() {
        //arrange
        String sessionId = "testSessionId";
        String wsSessionId = "testWsSessionId";
        String userId = "testUserId";
        JoinRequestDto joinRequestDto = JoinRequestDto.builder()
                .sessionId(sessionId)
                .templateId("testTemplateId")
                .username("testUsername")
                .build();
        Principal principal = Mockito.mock(Principal.class);

        JoinResponse successfulResponse = JoinResponse.newBuilder()
                .setSuccess(false)
                .build();

        Mockito.when(principal.getName()).thenReturn(userId);
        Mockito.when(gameSessionClient.joinSession(any())).thenReturn(successfulResponse);
        //act
        dispatcher.dispatchJoinSession(joinRequestDto, principal, wsSessionId);
        //assert
        Mockito.verify(connectionRegistry, Mockito.times(0)).register(wsSessionId, userId, sessionId);
    }
}
