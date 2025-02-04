package com.stool.studentcooperationtools.exception.global;

public class PrincipalNotFoundException extends IllegalStateException{

    public PrincipalNotFoundException(final String message) {
        super(message);
    }
}
