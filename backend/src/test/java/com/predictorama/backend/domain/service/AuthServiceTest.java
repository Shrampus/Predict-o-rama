package com.predictorama.backend.domain.service;

import com.predictorama.backend.domain.entity.Role;
import com.predictorama.backend.domain.entity.User;
import com.predictorama.backend.domain.exception.InvalidCredentialsException;
import com.predictorama.backend.domain.exception.UserNotFoundException;
import com.predictorama.backend.domain.port.PasswordVerifier;
import com.predictorama.backend.domain.port.persistence.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthServiceTest {

    private AuthService authService;
    private InMemoryUserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        authService = new AuthService(userRepository, new FakePasswordVerifier());
    }

    @Test
    void login_returnsUser_whenCredentialsAreValid() {
        var user = userWithHash("alice@test.com", "hashed:password123");
        userRepository.save(user);

        var result = authService.login("alice@test.com", "password123");

        assertThat(result.getEmail()).isEqualTo("alice@test.com");
    }

    @Test
    void login_throws_whenEmailNotFound() {
        assertThatThrownBy(() -> authService.login("unknown@test.com", "password123"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_throws_whenPasswordDoesNotMatch() {
        var user = userWithHash("alice@test.com", "hashed:password123");
        userRepository.save(user);

        assertThatThrownBy(() -> authService.login("alice@test.com", "wrongpassword"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_throws_whenPasswordHashIsNull() {
        var user = userWithHash("alice@test.com", null);
        userRepository.save(user);

        assertThatThrownBy(() -> authService.login("alice@test.com", "password123"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void getById_returnsUser_whenExists() {
        var user = userWithHash("alice@test.com", "hashed:password123");
        userRepository.save(user);

        var result = authService.getById(user.getId());

        assertThat(result.getId()).isEqualTo(user.getId());
    }

    @Test
    void getById_throws_whenNotFound() {
        assertThatThrownBy(() -> authService.getById(UUID.randomUUID()))
                .isInstanceOf(UserNotFoundException.class);
    }

    // --- Helpers ---

    private User userWithHash(String email, String hash) {
        return User.builder()
                .id(UUID.randomUUID())
                .username(email.split("@")[0])
                .email(email)
                .systemRole(Role.USER)
                .passwordHash(hash)
                .build();
    }

    // Fake: interprets encoded as "hashed:<raw>" so tests can control matching
    static class FakePasswordVerifier implements PasswordVerifier {
        @Override
        public boolean matches(String rawPassword, String encodedPassword) {
            return encodedPassword != null && encodedPassword.equals("hashed:" + rawPassword);
        }
    }

    static class InMemoryUserRepository implements UserRepositoryPort {
        private final Map<UUID, User> store = new HashMap<>();

        @Override
        public User save(User user) {
            store.put(user.getId(), user);
            return user;
        }

        @Override
        public Optional<User> findById(UUID id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public Optional<User> findByUsername(String username) {
            return store.values().stream().filter(u -> u.getUsername().equals(username)).findFirst();
        }

        @Override
        public Optional<User> findByEmail(String email) {
            return store.values().stream().filter(u -> u.getEmail().equals(email)).findFirst();
        }

        @Override
        public boolean existsByUsername(String username) {
            return store.values().stream().anyMatch(u -> u.getUsername().equals(username));
        }

        @Override
        public boolean existsByEmail(String email) {
            return store.values().stream().anyMatch(u -> u.getEmail().equals(email));
        }
    }
}
