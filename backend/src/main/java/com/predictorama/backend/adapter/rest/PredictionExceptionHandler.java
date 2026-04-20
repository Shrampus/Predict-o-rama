package com.predictorama.backend.adapter.rest;

import com.predictorama.backend.domain.exception.InvalidPredictionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PredictionExceptionHandler {

    @ExceptionHandler(InvalidPredictionException.class)
    public ResponseEntity<String> handleInvalidPrediction(InvalidPredictionException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}

