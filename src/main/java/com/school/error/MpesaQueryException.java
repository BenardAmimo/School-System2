package com.school.error;

/** Daraja answered the STK query with an HTTP error. This is "no answer yet", never "failed". */
public class MpesaQueryException extends MpesaException {

    private final String errorCode;

    public MpesaQueryException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /** Daraja's own code, e.g. "500.001.1001" ("The transaction does not Exist"). May be null. */
    public String getErrorCode() {
        return errorCode;
    }
}