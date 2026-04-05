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
        return Optional.ofNullable((UUID) session.getAttribute(USER_ID_KEY));
    }

    public void invalidate(HttpSession session) {
        session.invalidate();
    }
}
