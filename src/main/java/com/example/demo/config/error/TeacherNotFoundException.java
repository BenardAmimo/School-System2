package com.example.demo.config.error;

public class TeacherNotFoundException extends RuntimeException{
    public TeacherNotFoundException() {
        super();
    }

    public TeacherNotFoundException(String message) {
        super(message);
    }

    public TeacherNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public TeacherNotFoundException(Throwable cause) {
        super(cause);
    }
}
