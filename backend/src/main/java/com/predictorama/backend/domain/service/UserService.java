package com.predictorama.backend.domain.service;

import com.predictorama.backend.domain.entity.Role;
import com.predictorama.backend.domain.entity.User;
import com.predictorama.backend.domain.port.persistence.UserRepositoryPort;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class UserService {

    private final UserRepositoryPort userRepository;
    // TODO: remove default password once real auth (Google OAuth) is in place
    private final String defaultPasswordHash;

    public User createUser(String username, String email) {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username(username)
                .email(email)
                .systemRole(Role.USER)
                .passwordHash(defaultPasswordHash)
                .build();
        return userRepository.save(user);
    }
}
