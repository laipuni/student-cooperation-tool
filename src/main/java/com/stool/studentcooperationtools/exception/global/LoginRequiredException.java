package com.stool.studentcooperationtools.exception.global;

import org.springframework.security.core.AuthenticationException;

public class LoginRequiredException extends AuthenticationException {
    public LoginRequiredException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    public LoginRequiredException(final String msg) {
        super(msg);
    }
}
