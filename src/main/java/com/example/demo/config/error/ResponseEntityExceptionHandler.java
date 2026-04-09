package com.example.demo.config.error;

import jakarta.servlet.http.HttpServlet;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
@ResponseStatus
public class ResponseEntityExceptionHandler extends org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler {

    @ExceptionHandler(CourseNotFoundException.class)
    public ResponseEntity<ErrorMessage>courseNotFoundException
            (CourseNotFoundException exception , WebRequest request){

        ErrorMessage funny = new ErrorMessage(HttpStatus.NOT_FOUND,
                exception.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(funny);
    }

    @ExceptionHandler(StudentNotFoundtException.class)
    public ResponseEntity<ErrorMessage> studentNotFoundException(StudentNotFoundtException exception, HttpServlet httpServlet){
        ErrorMessage err = new ErrorMessage(HttpStatus.NOT_FOUND,exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
    }
}
