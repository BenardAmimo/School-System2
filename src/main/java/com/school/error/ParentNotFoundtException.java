package com.school.error;

public class ParentNotFoundtException extends RuntimeException{
    public ParentNotFoundtException(String message) {
        super(message);
    }

    public ParentNotFoundtException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParentNotFoundtException(Throwable cause) {
        super(cause);
    }

    protected ParentNotFoundtException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ParentNotFoundtException() {
        super();


    }
}
