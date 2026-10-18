package com.school.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class ErrorHandling{

    @ExceptionHandler(SubjectNotFoundException.class)
    public ResponseEntity<ErrorMessage> courseNotFoundException(
            SubjectNotFoundException courseNotFoundException,
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


    @ExceptionHandler(ParentNotFoundtException.class)
    public ResponseEntity<ErrorMessage> parentNotFoundException(
            ParentNotFoundtException parentNotFoundtException,
            WebRequest request){

        ErrorMessage message = new ErrorMessage(HttpStatus.NOT_FOUND,
                parentNotFoundtException.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
    }

    @ExceptionHandler(MpesaException.class)
    public ResponseEntity<ErrorMessage> mpesaException(MpesaException mpesaException, WebRequest request){

        ErrorMessage error = new ErrorMessage(HttpStatus.NOT_FOUND,mpesaException.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(SchoolClassesNotFoundException.class)
    public ResponseEntity<ErrorMessage> SchoolClassesNotFoundException(
            SchoolClassesNotFoundException schoolClassesNotFoundException,
            WebRequest request){

        ErrorMessage message = new ErrorMessage(HttpStatus.NOT_FOUND,
                schoolClassesNotFoundException.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
    }
//checked
    record Err(String message) {}

    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
    public ResponseEntity<Err> bad(RuntimeException ex) {
        return ResponseEntity.badRequest().body(new Err(ex.getMessage()));
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Err> conflict() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new Err("This record was changed by someone else. Reload and try again."));
    }


}
