package com.school.error;

public class MpesaException extends RuntimeException{
    public MpesaException() {
        super();
    }

    public MpesaException(String message) {
        super(message);
    }

    public MpesaException(String message, Throwable cause) {
        super(message, cause);
    }
}
