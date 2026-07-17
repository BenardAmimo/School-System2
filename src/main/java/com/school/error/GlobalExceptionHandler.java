package com.school.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MpesaException.class)
    public ResponseEntity<ErrorMessage> mpesaException(MpesaException mpesaException, WebRequest request){

        ErrorMessage error = new ErrorMessage(HttpStatus.NOT_FOUND,mpesaException.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}
