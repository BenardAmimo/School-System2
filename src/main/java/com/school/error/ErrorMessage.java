package com.school.error;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Data
@ResponseStatus
@AllArgsConstructor
public class ErrorMessage {
    private HttpStatus status;
    private String message;

}
