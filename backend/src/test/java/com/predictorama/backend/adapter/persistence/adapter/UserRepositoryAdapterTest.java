package com.predictorama.backend.adapter.persistence.adapter;

import com.predictorama.backend.adapter.persistence.entity.UserEntity;
import com.predictorama.backend.adapter.persistence.repository.UserJpaRepository;
import com.predictorama.backend.domain.entity.Role;
import com.predictorama.backend.domain.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRepositoryAdapterTest {

    @Mock
    private UserJpaRepository jpaRepository;

    @InjectMocks
    private UserRepositoryAdapter adapter;

    @Test
    void save_mapsToEntityPersistsAndReturnsDomain() {
        UUID id = UUID.randomUUID();
        User user = User.builder().id(id).username("alice").email("alice@example.com").systemRole(Role.USER).build();
        UserEntity entity = UserEntity.builder().id(id).username("alice").email("alice@example.com").systemRole(Role.USER).build();
        when(jpaRepository.save(any(UserEntity.class))).thenReturn(entity);

        User result = adapter.save(user);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getUsername()).isEqualTo("alice");
        verify(jpaRepository).save(any(UserEntity.class));
    }

    @Test
    void findById_returnsMappedDomainWhenFound() {
        UUID id = UUID.randomUUID();
        UserEntity entity = UserEntity.builder().id(id).username("alice").email("alice@example.com").systemRole(Role.USER).build();
        when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));

        Optional<User> result = adapter.findById(id);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(id);
    }

    @Test
    void findById_returnsEmptyWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(jpaRepository.findById(id)).thenReturn(Optional.empty());

        Optional<User> result = adapter.findById(id);

        assertThat(result).isEmpty();
    }

    @Test
    void findByUsername_returnsMappedWhenFound() {
        UUID id = UUID.randomUUID();
        UserEntity entity = UserEntity.builder().id(id).username("alice").email("alice@example.com").systemRole(Role.USER).build();
        when(jpaRepository.findByUsername("alice")).thenReturn(Optional.of(entity));

        Optional<User> result = adapter.findByUsername("alice");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("alice");
    }

    @Test
    void findByUsername_returnsEmptyWhenNotFound() {
        when(jpaRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThat(adapter.findByUsername("unknown")).isEmpty();
    }

    @Test
    void findByEmail_returnsMappedWhenFound() {
        UUID id = UUID.randomUUID();
        UserEntity entity = UserEntity.builder().id(id).username("alice").email("alice@example.com").systemRole(Role.USER).build();
        when(jpaRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(entity));

        Optional<User> result = adapter.findByEmail("alice@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void existsByUsername_delegatesToJpa() {
        when(jpaRepository.existsByUsername("alice")).thenReturn(true);

        assertThat(adapter.existsByUsername("alice")).isTrue();
        verify(jpaRepository).existsByUsername("alice");
    }

    @Test
    void existsByEmail_delegatesToJpa() {
        when(jpaRepository.existsByEmail("alice@example.com")).thenReturn(false);

        assertThat(adapter.existsByEmail("alice@example.com")).isFalse();
        verify(jpaRepository).existsByEmail("alice@example.com");
    }
}
