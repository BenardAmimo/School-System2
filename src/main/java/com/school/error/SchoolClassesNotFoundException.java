package com.school.error;

public class SchoolClassesNotFoundException extends RuntimeException {
    public SchoolClassesNotFoundException(String message) {
        super(message);
    }
}
