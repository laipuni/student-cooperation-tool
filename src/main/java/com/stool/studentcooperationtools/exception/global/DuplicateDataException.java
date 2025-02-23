package com.stool.studentcooperationtools.exception.global;

public class DuplicateDataException extends RuntimeException{
    public DuplicateDataException(final String message) {
        super(message);
    }

    public DuplicateDataException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
