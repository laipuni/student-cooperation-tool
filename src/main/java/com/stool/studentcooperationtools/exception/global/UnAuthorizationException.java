package com.stool.studentcooperationtools.exception.global;

public class UnAuthorizationException extends RuntimeException{

    public UnAuthorizationException(final String message) {
        super(message);
    }

    public UnAuthorizationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
