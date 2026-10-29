package com.school.money.web;

import com.school.money.MoneyException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/** Only handles MoneyException, so it cannot interfere with the project's global handler. */
@RestControllerAdvice(basePackages = "com.school.money")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MoneyExceptionHandler {

    @ExceptionHandler(MoneyException.class)
    public ResponseEntity<Map<String, Object>> handle(MoneyException e) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", e.getStatus().value());
        body.put("error", e.getStatus().getReasonPhrase());
        body.put("message", e.getMessage());
        return ResponseEntity.status(e.getStatus()).body(body);
    }
}
