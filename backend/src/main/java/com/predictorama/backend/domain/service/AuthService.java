package com.predictorama.backend.domain.service;

import com.predictorama.backend.domain.entity.User;
import com.predictorama.backend.domain.exception.InvalidCredentialsException;
import com.predictorama.backend.domain.exception.UserNotFoundException;
import com.predictorama.backend.domain.port.PasswordVerifier;
import com.predictorama.backend.domain.port.persistence.UserRepositoryPort;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class AuthService {

    private final UserRepositoryPort userRepository;
    private final PasswordVerifier passwordVerifier;

    public User login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);
        if (user.getPasswordHash() == null || !passwordVerifier.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return user;
    }

    public User getById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
}
