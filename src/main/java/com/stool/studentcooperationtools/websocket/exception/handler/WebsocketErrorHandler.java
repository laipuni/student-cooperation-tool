package com.stool.studentcooperationtools.websocket.exception.handler;

import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

@Component
public class WebsocketErrorHandler extends StompSubProtocolErrorHandler {
    @Override
    public Message<byte[]> handleClientMessageProcessingError(final Message<byte[]> clientMessage, final Throwable ex) {
        return super.handleClientMessageProcessingError(clientMessage, ex);
    }
}
