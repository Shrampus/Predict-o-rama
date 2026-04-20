package com.predictorama.backend.adapter.rest;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class SessionService {

    private static final String USER_ID_KEY = "userId";

    public void setUserId(HttpSession session, UUID userId) {
        session.setAttribute(USER_ID_KEY, userId);
    }

    public Optional<UUID> getUserId(HttpSession session) {
        Object value = session.getAttribute(USER_ID_KEY);

        if (value == null) {
            return Optional.empty();
        }

        if (value instanceof UUID uuid) {
            return Optional.of(uuid);
        }

        return Optional.of(UUID.fromString(value.toString()));
    }

    public UUID getUserIdOrThrow(HttpSession session) {
        return getUserId(session)
                .orElseThrow(() -> new IllegalStateException("User is not authenticated."));
    }

    public void invalidate(HttpSession session) {
        session.invalidate();
    }
}