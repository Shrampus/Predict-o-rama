package com.predictorama.backend.domain.port;

public interface PasswordVerifier {
    boolean matches(String rawPassword, String encodedPassword);
}
