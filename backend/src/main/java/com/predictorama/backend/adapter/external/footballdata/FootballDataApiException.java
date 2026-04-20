package com.predictorama.backend.adapter.external.footballdata;

public class FootballDataApiException extends RuntimeException {

    public FootballDataApiException(String message) {
        super(message);
    }

    public FootballDataApiException(String message, Throwable cause) {
        super(message, cause);
    }
}