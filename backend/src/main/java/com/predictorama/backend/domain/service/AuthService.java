package com.predictorama.backend.domain.service;

import com.predictorama.backend.domain.entity.GoogleUserInfo;
import com.predictorama.backend.domain.entity.Role;
import com.predictorama.backend.domain.entity.User;
import com.predictorama.backend.domain.exception.InvalidCredentialsException;
import com.predictorama.backend.domain.exception.UserNotFoundException;
import com.predictorama.backend.domain.exception.UsernameTakenException;
import com.predictorama.backend.domain.port.PasswordVerifier;
import com.predictorama.backend.domain.port.external.GoogleTokenValidatorPort;
import com.predictorama.backend.domain.port.persistence.UserRepositoryPort;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class AuthService {

    private final UserRepositoryPort userRepository;
    private final GoogleTokenValidatorPort googleTokenValidatorPort;
    private final PasswordVerifier passwordVerifier;

    public AuthResult login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);
        if (user.getPasswordHash() == null || !passwordVerifier.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return new AuthResult(user, user.getUsername() == null);
    }

    public AuthResult loginWithGoogle(String idToken) {
        GoogleUserInfo googleUserInfo = googleTokenValidatorPort.validate(idToken);
        Optional<User> existing = userRepository.findByGoogleId(googleUserInfo.googleId());
        if (existing.isPresent()) {
            return new AuthResult(existing.get(), existing.get().getUsername() == null);
        }
        User newUser = userRepository.save(User.builder()
                .id(UUID.randomUUID())
                .email(googleUserInfo.email())
                .username(null)
                .googleId(googleUserInfo.googleId())
                .systemRole(Role.USER)
                .build());
        return new AuthResult(newUser, true);
    }

    public User completeProfile(UUID userId, String username) {
        User user = userRepository.findById(userId).orElseThrow();
        if (userRepository.existsByUsername(username)) {
            throw new UsernameTakenException(username);
        };
        user.setUsername(username);
        userRepository.save(user);
        return user;
    }
    public User getById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }



}
