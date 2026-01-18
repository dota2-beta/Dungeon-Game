package com.abs.dungeoncrawler.websocketservice;

import com.abs.dungeoncrawler.websocketservice.dto.JoinRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.security.Principal;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketIntegrationTest {
    @LocalServerPort
    private int port;

    @MockitoBean
    private ActionDispatcher dispatcher;

    private WebSocketStompClient stompClient;

    @BeforeEach
    public void setUp() {
        List<Transport> transports = List.of(new WebSocketTransport(new StandardWebSocketClient()));
        SockJsClient sockJsClient = new SockJsClient(transports);

        this.stompClient = new WebSocketStompClient(sockJsClient);
        this.stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @Test
    void testJoinSession() throws ExecutionException, InterruptedException, TimeoutException {
        //arrange
        String url = "ws://localhost:" + port + "/gs-websocket";

        StompSession session = stompClient
                .connectAsync(url, new StompSessionHandlerAdapter() {})
                .get(1, TimeUnit.SECONDS);

        String sessionId = "session-1";
        JoinRequestDto request = JoinRequestDto.builder()
                .sessionId(sessionId)
                .templateId("warrior")
                .username("test_user")
                .build();

        //act
        session.send("/app/join-session", request);
        //assert
        verify(dispatcher, timeout(1000).times(1))
                .dispatchJoinSession(eq(request), nullable(Principal.class), any(String.class));
    }
}
