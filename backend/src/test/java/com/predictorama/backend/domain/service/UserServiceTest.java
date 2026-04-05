package com.predictorama.backend.domain.service;

import com.predictorama.backend.domain.entity.Role;
import com.predictorama.backend.domain.entity.User;
import com.predictorama.backend.domain.port.persistence.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class UserServiceTest {

    private UserService userService;
    private InMemoryUserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        userService = new UserService(userRepository, "hashed-default");
    }

    @Test
    void createUser_savesUserWithGeneratedId() {
        User user = userService.createUser("alice", "alice@example.com");

        assertThat(user.getId()).isNotNull();
        assertThat(user.getUsername()).isEqualTo("alice");
        assertThat(user.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void createUser_assignsUserRole() {
        User user = userService.createUser("alice", "alice@example.com");

        assertThat(user.getSystemRole()).isEqualTo(Role.USER);
    }

    @Test
    void createUser_persistsToRepository() {
        User user = userService.createUser("alice", "alice@example.com");

        assertThat(userRepository.findById(user.getId())).isPresent();
    }

    @Test
    void createUser_eachCallGeneratesUniqueId() {
        User first = userService.createUser("alice", "alice@example.com");
        User second = userService.createUser("bob", "bob@example.com");

        assertThat(first.getId()).isNotEqualTo(second.getId());
    }

    // --- In-memory stub ---

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
