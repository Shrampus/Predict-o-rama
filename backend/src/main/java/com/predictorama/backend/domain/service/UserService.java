package com.predictorama.backend.domain.service;

import com.predictorama.backend.domain.entity.Role;
import com.predictorama.backend.domain.entity.User;
import com.predictorama.backend.domain.port.persistence.UserRepositoryPort;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class UserService {

    private final UserRepositoryPort userRepository;

    public User createUser(String username, String email) {
        User user = new User(UUID.randomUUID(), username, email, Role.USER);
        return userRepository.save(user);
    }
}
