package com.predictorama.backend.adapter.persistence.mapper;

import com.predictorama.backend.adapter.persistence.entity.UserEntity;
import com.predictorama.backend.domain.entity.Role;
import com.predictorama.backend.domain.entity.User;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    @Test
    void toEntity_mapsAllFields() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("alice")
                .email("alice@example.com")
                .systemRole(Role.USER)
                .build();

        UserEntity entity = UserMapper.toEntity(user);

        assertThat(entity.getId()).isEqualTo(user.getId());
        assertThat(entity.getUsername()).isEqualTo(user.getUsername());
        assertThat(entity.getEmail()).isEqualTo(user.getEmail());
        assertThat(entity.getSystemRole()).isEqualTo(user.getSystemRole());
    }

    @Test
    void toDomain_mapsAllFields() {
        UserEntity entity = UserEntity.builder()
                .id(UUID.randomUUID())
                .username("alice")
                .email("alice@example.com")
                .systemRole(Role.ADMIN)
                .build();

        User user = UserMapper.toDomain(entity);

        assertThat(user.getId()).isEqualTo(entity.getId());
        assertThat(user.getUsername()).isEqualTo(entity.getUsername());
        assertThat(user.getEmail()).isEqualTo(entity.getEmail());
        assertThat(user.getSystemRole()).isEqualTo(entity.getSystemRole());
    }

    @Test
    void roundTrip_domainToEntityToDomain_preservesAllFields() {
        User original = User.builder()
                .id(UUID.randomUUID())
                .username("bob")
                .email("bob@example.com")
                .systemRole(Role.USER)
                .build();

        User result = UserMapper.toDomain(UserMapper.toEntity(original));

        assertThat(result.getId()).isEqualTo(original.getId());
        assertThat(result.getUsername()).isEqualTo(original.getUsername());
        assertThat(result.getEmail()).isEqualTo(original.getEmail());
        assertThat(result.getSystemRole()).isEqualTo(original.getSystemRole());
    }
}
