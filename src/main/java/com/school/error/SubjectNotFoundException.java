package com.school.error;

public class SubjectNotFoundException extends Exception{
    public SubjectNotFoundException() {
        super();
    }

    public SubjectNotFoundException(String message) {
        super(message);
    }

    public SubjectNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public SubjectNotFoundException(Throwable cause) {
        super(cause);
    }

    protected SubjectNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
