package com.predictorama.backend.domain.exception;

public class UsernameTakenException extends RuntimeException {
    public UsernameTakenException(String username) {
        super("Username " + username + " already taken.");
    }
}
