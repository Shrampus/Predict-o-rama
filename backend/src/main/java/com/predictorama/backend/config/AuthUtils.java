package com.predictorama.backend.config;

import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public final class AuthUtils {

    private AuthUtils() {
    }

    public static UUID currentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalStateException("No authenticated user in context");
        }

        return UUID.fromString((String) auth.getPrincipal());
    }
}
