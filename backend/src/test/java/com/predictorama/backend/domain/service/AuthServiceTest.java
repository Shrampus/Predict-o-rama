package com.predictorama.backend.domain.service;

import com.predictorama.backend.domain.entity.GoogleUserInfo;
import com.predictorama.backend.domain.entity.Role;
import com.predictorama.backend.domain.entity.User;
import com.predictorama.backend.domain.exception.InvalidCredentialsException;
import com.predictorama.backend.domain.exception.InvalidGoogleTokenException;
import com.predictorama.backend.domain.exception.UserNotFoundException;
import com.predictorama.backend.domain.exception.UsernameTakenException;
import com.predictorama.backend.domain.port.PasswordVerifier;
import com.predictorama.backend.domain.port.external.GoogleTokenValidatorPort;
import com.predictorama.backend.domain.port.persistence.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthServiceTest {

    private AuthService authService;
    private InMemoryUserRepository userRepository;
    private FakeGoogleTokenValidator googleTokenValidator;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        googleTokenValidator = new FakeGoogleTokenValidator();
        authService = new AuthService(userRepository, googleTokenValidator, new FakePasswordVerifier());
    }

    // --- login ---

    @Test
    void login_returnsUser_whenCredentialsAreValid() {
        var user = userWithHash("alice@test.com", "hashed:password123");
        userRepository.save(user);

        var result = authService.login("alice@test.com", "password123");

        assertThat(result.user().getEmail()).isEqualTo("alice@test.com");
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

    // --- loginWithGoogle ---

    @Test
    void loginWithGoogle_createsNewUser_whenGoogleIdNotFound() {
        googleTokenValidator.returnInfo = new GoogleUserInfo("google-123", "new@test.com", "New User");

        var result = authService.loginWithGoogle("valid-token");

        assertThat(result.user().getEmail()).isEqualTo("new@test.com");
        assertThat(result.user().getGoogleId()).isEqualTo("google-123");
        assertThat(result.user().getUsername()).isNull();
        assertThat(result.needsOnboarding()).isTrue();
    }

    @Test
    void loginWithGoogle_returnsExistingUser_whenGoogleIdAlreadyLinked() {
        var existing = userWithGoogleId("bob@test.com", "google-456", "bob");
        userRepository.save(existing);
        googleTokenValidator.returnInfo = new GoogleUserInfo("google-456", "bob@test.com", "Bob");

        var result = authService.loginWithGoogle("valid-token");

        assertThat(result.user().getId()).isEqualTo(existing.getId());
        assertThat(result.needsOnboarding()).isFalse();
    }

    @Test
    void loginWithGoogle_setsNeedsOnboarding_whenExistingUserHasNoUsername() {
        var existing = userWithGoogleId("bob@test.com", "google-456", null);
        userRepository.save(existing);
        googleTokenValidator.returnInfo = new GoogleUserInfo("google-456", "bob@test.com", "Bob");

        var result = authService.loginWithGoogle("valid-token");

        assertThat(result.needsOnboarding()).isTrue();
    }

    @Test
    void loginWithGoogle_throws_whenTokenIsInvalid() {
        googleTokenValidator.throwException = true;

        assertThatThrownBy(() -> authService.loginWithGoogle("bad-token"))
                .isInstanceOf(InvalidGoogleTokenException.class);
    }

    // --- completeProfile ---

    @Test
    void completeProfile_setsUsername_whenAvailable() {
        var user = userWithGoogleId("carol@test.com", "google-789", null);
        userRepository.save(user);

        authService.completeProfile(user.getId(), "carol");

        assertThat(userRepository.findById(user.getId()).get().getUsername()).isEqualTo("carol");
    }

    @Test
    void completeProfile_throws_whenUsernameTaken() {
        var user1 = userWithHash("alice@test.com", "hashed:pass");
        user1.setUsername("alice");
        userRepository.save(user1);

        var user2 = userWithGoogleId("carol@test.com", "google-789", null);
        userRepository.save(user2);

        assertThatThrownBy(() -> authService.completeProfile(user2.getId(), "alice"))
                .isInstanceOf(UsernameTakenException.class);
    }

    @Test
    void completeProfile_throws_whenUserNotFound() {
        assertThatThrownBy(() -> authService.completeProfile(UUID.randomUUID(), "newname"))
                .isInstanceOf(NoSuchElementException.class);
    }

    // --- getById ---

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

    private User userWithGoogleId(String email, String googleId, String username) {
        return User.builder()
                .id(UUID.randomUUID())
                .username(username)
                .email(email)
                .googleId(googleId)
                .systemRole(Role.USER)
                .build();
    }

    static class FakeGoogleTokenValidator implements GoogleTokenValidatorPort {
        GoogleUserInfo returnInfo;
        boolean throwException = false;

        @Override
        public GoogleUserInfo validate(String idToken) {
            if (throwException) throw new InvalidGoogleTokenException("invalid token");
            return returnInfo;
        }
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
            return store.values().stream().anyMatch(u -> username.equals(u.getUsername()));
        }

        @Override
        public boolean existsByEmail(String email) {
            return store.values().stream().anyMatch(u -> u.getEmail().equals(email));
        }

        @Override
        public Optional<User> findByGoogleId(String googleId) {
            return store.values().stream().filter(u -> googleId.equals(u.getGoogleId())).findFirst();
        }
    }
}
