package com.abs.dungeoncrawler.gamesessionservice;

import com.abs.dungeoncrawler.gamesessionservice.services.SessionLifecycleManager;
import com.dungeoncrawler.contracts.grpc.gamesession.GameSessionServiceGrpc;
import com.dungeoncrawler.contracts.grpc.gamesession.JoinRequest;
import com.dungeoncrawler.contracts.grpc.gamesession.JoinResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.grpc.server.lifecycle.GrpcServerLifecycle;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = "spring.grpc.server.port=0")
@ActiveProfiles("test")
public class GameSessionEndpointTest {
    @Autowired
    private GrpcServerLifecycle grpcServerLifecycle;

    @MockitoBean
    private SessionLifecycleManager sessionLifecycleManager;

    private ManagedChannel channel;
    private GameSessionServiceGrpc.GameSessionServiceBlockingStub blockingStub;

    @BeforeEach
    void setUp() {
        int port = grpcServerLifecycle.getPort();
        channel = ManagedChannelBuilder.forAddress("localhost", port)
                .usePlaintext()
                .build();

        blockingStub = GameSessionServiceGrpc.newBlockingStub(channel);
    }

    @AfterEach
    void tearDown() {
        if (channel != null) {
            channel.shutdown();
        }
    }

    @Test
    void shouldReceiveAndProcessJoinRequest() {
        //arrange
        JoinRequest joinRequest = JoinRequest.newBuilder()
                .setSessionId("testSessionId")
                .setTemplateId("testTemplateId")
                .setUsername("testUsername")
                .setUserId("testUserId")
                .build();
        JoinResponse response = JoinResponse.newBuilder()
                .setSuccess(true)
                .build();
        when(sessionLifecycleManager.handleJoinSession(any(JoinRequest.class))).thenReturn(response);
        //act
        JoinResponse stubResponse = blockingStub.joinSession(joinRequest);
        //assert
        assertTrue(stubResponse.getSuccess(), "Response should be successful");
    }
}
