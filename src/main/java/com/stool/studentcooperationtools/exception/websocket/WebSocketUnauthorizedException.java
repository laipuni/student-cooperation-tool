package com.stool.studentcooperationtools.exception.websocket;

import org.springframework.security.access.AccessDeniedException;

public class WebSocketUnauthorizedException extends AccessDeniedException {
    public WebSocketUnauthorizedException(final String msg) {
        super(msg);
    }

    public WebSocketUnauthorizedException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
