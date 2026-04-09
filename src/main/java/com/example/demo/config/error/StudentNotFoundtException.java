package com.example.demo.config.error;

public class StudentNotFoundtException extends RuntimeException{
    public StudentNotFoundtException(String message) {
        super(message);
    }

    public StudentNotFoundtException(String message, Throwable cause) {
        super(message, cause);
    }

    public StudentNotFoundtException(Throwable cause) {
        super(cause);
    }

    protected StudentNotFoundtException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public StudentNotFoundtException() {
        super();


    }
}
