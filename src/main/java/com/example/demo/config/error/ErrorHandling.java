package com.example.demo.config.error;

import jakarta.persistence.Entity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class ErrorHandling{

    @ExceptionHandler(CourseNotFoundException.class)
    public ResponseEntity<ErrorMessage> courseNotFoundException(
            CourseNotFoundException courseNotFoundException,
            WebRequest request){

        ErrorMessage message = new ErrorMessage(HttpStatus.NOT_FOUND,
                courseNotFoundException.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
    }


    @ExceptionHandler(TeacherNotFoundException.class)
    public ResponseEntity<ErrorMessage> teacherNotFoundException(
            TeacherNotFoundException teacherNotFoundException,
            WebRequest request
    ){

        ErrorMessage message = new ErrorMessage(HttpStatus.NOT_FOUND,
                teacherNotFoundException.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
    }


    @ExceptionHandler(StudentNotFoundtException.class)
    public ResponseEntity<ErrorMessage> studentNotFoundException(
            StudentNotFoundtException studentNotFoundException,
            WebRequest request){

        ErrorMessage message = new ErrorMessage(HttpStatus.NOT_FOUND,
                studentNotFoundException.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
    }

}
