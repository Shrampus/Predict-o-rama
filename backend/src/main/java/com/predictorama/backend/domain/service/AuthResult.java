package com.predictorama.backend.domain.service;

import com.predictorama.backend.domain.entity.User;

public record AuthResult(User user, boolean needsOnboarding) {}