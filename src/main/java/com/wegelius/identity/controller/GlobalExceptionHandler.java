package com.wegelius.identity.controller;

import com.wegelius.identity.exception.EmailAlreadyExistsException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<String> handleDuplicateEmail(EmailAlreadyExistsException ex) {
        return ResponseEntity.status(409).body(ex.getMessage());
    }
}
