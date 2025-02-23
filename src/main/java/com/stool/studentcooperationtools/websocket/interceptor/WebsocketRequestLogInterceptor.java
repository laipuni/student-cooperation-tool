package com.stool.studentcooperationtools.websocket.interceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 98)
public class WebsocketRequestLogInterceptor implements ChannelInterceptor {
    @Override
    public Message<?> preSend(final Message<?> message, final MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        log.debug("[Websocket Reqeust] type = {}, path = {}, message = {}",
                accessor.getMessageType(),accessor.getDestination(),getPayload(message));
        return message;
    }

    private static String getPayload(final Message<?> message) {
        Object payload = message.getPayload();
        String payloadString = (payload instanceof byte[]) ? new String((byte[]) payload) : payload.toString();
        return StringUtils.hasText(payloadString) ? payloadString : "{}";
    }


}
