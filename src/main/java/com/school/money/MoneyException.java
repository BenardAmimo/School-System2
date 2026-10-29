package com.school.money;

import org.springframework.http.HttpStatus;

public class MoneyException extends RuntimeException {
    private final HttpStatus status;

    public MoneyException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() { return status; }

    public static MoneyException bad(String m) { return new MoneyException(HttpStatus.BAD_REQUEST, m); }
    public static MoneyException conflict(String m) { return new MoneyException(HttpStatus.CONFLICT, m); }
    public static MoneyException notFound(String m) { return new MoneyException(HttpStatus.NOT_FOUND, m); }
    public static MoneyException forbidden(String m) { return new MoneyException(HttpStatus.FORBIDDEN, m); }
}