package com.stool.studentcooperationtools.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.stool.studentcooperationtools.domain.room.repository.RoomRepository;
import com.stool.studentcooperationtools.websocket.controller.request.WebsocketResponse;
import com.stool.studentcooperationtools.websocket.converter.SessionMemberMessageConverter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.stool.studentcooperationtools.security.config.SecurityConfig.SESSION_NAME;
import static com.stool.studentcooperationtools.websocket.interceptor.WebsocketSecurityInterceptor.SUB_URL_HEADER;

@ActiveProfiles("test")
@TestPropertySource(properties = "spring.config.location=classpath:application.yml")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class WebsocketTestSupport {

    @LocalServerPort
    int port;

    @Autowired
    protected DataSource dataSource;
    protected String URL;
    protected StompSession stompSession;
    protected WebSocketStompClient stompClient;
    protected final CustomSessionHandlerAdapter<WebsocketResponse> resultHandler = new CustomSessionHandlerAdapter<>(WebsocketResponse.class);

    @MockBean
    SessionMemberMessageConverter sessionMemberMessageConverter;

    @MockBean
    RoomRepository roomRepository;

    @BeforeEach
    void setUp() throws ExecutionException, InterruptedException, TimeoutException, JsonProcessingException {
        // validMemberInRoom 메서드를 스텁하여 실제 로직을 실행하지 않도록 설정
        Mockito.when(roomRepository.existMemberInRoom(Mockito.anyString(),Mockito.anyLong()))
                .thenReturn(true);
        URL = "ws://localhost:%d/ws-stomp".formatted(port);
        stompClient = createStompClient();
        executeSql("sql/SpringSessionDelete.sql");
        executeSql("sql/SpringSessionCreate.sql");
        StompHeaders stompHeaders = new StompHeaders();
        stompHeaders.add(SESSION_NAME,"testSession");
        stompHeaders.add(SUB_URL_HEADER,"/sub/rooms/10/");
        stompHeaders.add("email","email@email.com");
        connect(stompHeaders);
    }

    private void connect(final StompHeaders stompHeaders) throws InterruptedException, ExecutionException {
        try {
            stompSession = stompClient.connectAsync(URL, new WebSocketHttpHeaders(), stompHeaders, new StompSessionHandlerAdapter() {})
                    .get(10, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new IllegalStateException("STOMP 연결 시간 초과", e);
        }
    }

    @AfterEach
    void tearDown(){
        if(stompSession.isConnected()){
            stompSession.disconnect();
        }
        executeSql("sql/SpringSessionDelete.sql");
    }

    private WebSocketStompClient createStompClient(){
        WebSocketStompClient webSocketStompClient = new WebSocketStompClient(
                new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient()))));
        MappingJackson2MessageConverter messageConverter = new MappingJackson2MessageConverter();
        ObjectMapper objectMapper = messageConverter.getObjectMapper();
        objectMapper.registerModules(new JavaTimeModule(), new ParameterNamesModule());
        webSocketStompClient.setMessageConverter(messageConverter);
        return webSocketStompClient;
    }

    private void executeSql(String sqlFilePath) {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource(sqlFilePath));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
