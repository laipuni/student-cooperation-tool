package com.stool.studentcooperationtools.websocket.interceptor;

import com.stool.studentcooperationtools.domain.room.repository.RoomRepository;
import com.stool.studentcooperationtools.exception.global.LoginRequiredException;
import com.stool.studentcooperationtools.exception.websocket.WebSocketUnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.session.jdbc.JdbcIndexedSessionRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.stool.studentcooperationtools.security.config.SecurityConfig.SESSION_NAME;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebsocketSecurityInterceptor implements ChannelInterceptor {

    public static final String SUB_URL_HEADER = "SubscribeUrl";
    private final JdbcIndexedSessionRepository jdbcIndexedSessionRepository;
    private final RoomRepository roomRepository;
    @Override
    public Message<?> preSend(final Message<?> message, final MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        OAuth2AuthenticationToken authentication = (OAuth2AuthenticationToken) accessor.getHeader("simpUser");
        if(SimpMessageType.CONNECT.equals(accessor.getMessageType())){
            validSession(authentication, accessor);
            validAuthorization(accessor, authentication);
        }
        return message;
    }

    public void validSession(final Authentication authentication, final StompHeaderAccessor accessor) {
        // 웹소켓 연결 전에 인증하지 않은 경우 authentication는 조회되지 않음
        if(authentication == null){
            String jsessionid = accessor.getFirstNativeHeader(SESSION_NAME);
            Optional.ofNullable(jdbcIndexedSessionRepository.findById(jsessionid))
                    .orElseThrow(() -> new LoginRequiredException("로그인이 필요합니다."));
        }
    }

    public void validAuthorization(final StompHeaderAccessor accessor, final OAuth2AuthenticationToken authentication) {
        //방에 들어갈 권한이 없는 경우
        String subUrl = accessor.getFirstNativeHeader(SUB_URL_HEADER);
        Long roomId =  getRoomIdBy(subUrl);
        String email = getEmail(accessor, authentication);
        if(!roomRepository.existMemberInRoom(email, roomId)){
            throw new WebSocketUnauthorizedException("방에 참여할 권한이 없습니다.");
        }
    }

    private String getEmail(final StompHeaderAccessor accessor, final OAuth2AuthenticationToken authentication){
        if(authentication != null){
            return authentication.getPrincipal().getAttribute("email");
        } else {
            return accessor.getFirstNativeHeader("email");
        }
    }
    private Long getRoomIdBy(final String destination){
        //방의 id를 추출하는 메소드
        return Long.valueOf(destination.split("/")[3]);
    }
}
