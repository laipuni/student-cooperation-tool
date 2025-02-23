package com.stool.studentcooperationtools.exception.websocket;

public class WebSocketUnauthorizedException extends RuntimeException {
    public WebSocketUnauthorizedException(final String msg) {
        super(msg);
    }

    public WebSocketUnauthorizedException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
