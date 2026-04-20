package com.predictorama.backend.domain.exception;

public class InvalidPredictionException extends RuntimeException {
    public InvalidPredictionException(String message) {
        super(message);
    }
}

