package com.predictorama.backend.adapter.persistence.mapper;

import com.predictorama.backend.adapter.persistence.entity.GroupEntity;
import com.predictorama.backend.domain.entity.Group;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GroupMapperTest {

    @Test
    void toEntity_mapsAllFields() {
        Group group = Group.builder()
                .id(UUID.randomUUID())
                .ownerId(UUID.randomUUID())
                .inviteCode(UUID.randomUUID())
                .name("Legends")
                .description("Best group")
                .build();

        GroupEntity entity = GroupMapper.toEntity(group);

        assertThat(entity.getId()).isEqualTo(group.getId());
        assertThat(entity.getOwnerId()).isEqualTo(group.getOwnerId());
        assertThat(entity.getInviteCode()).isEqualTo(group.getInviteCode());
        assertThat(entity.getName()).isEqualTo(group.getName());
        assertThat(entity.getDescription()).isEqualTo(group.getDescription());
    }

    @Test
    void toDomain_mapsAllFields() {
        GroupEntity entity = GroupEntity.builder()
                .id(UUID.randomUUID())
                .ownerId(UUID.randomUUID())
                .inviteCode(UUID.randomUUID())
                .name("Legends")
                .description("Best group")
                .build();

        Group group = GroupMapper.toDomain(entity);

        assertThat(group.getId()).isEqualTo(entity.getId());
        assertThat(group.getOwnerId()).isEqualTo(entity.getOwnerId());
        assertThat(group.getInviteCode()).isEqualTo(entity.getInviteCode());
        assertThat(group.getName()).isEqualTo(entity.getName());
        assertThat(group.getDescription()).isEqualTo(entity.getDescription());
    }

    @Test
    void roundTrip_domainToEntityToDomain_preservesAllFields() {
        Group original = Group.builder()
                .id(UUID.randomUUID())
                .ownerId(UUID.randomUUID())
                .inviteCode(UUID.randomUUID())
                .name("Legends")
                .description(null)
                .build();

        Group result = GroupMapper.toDomain(GroupMapper.toEntity(original));

        assertThat(result.getId()).isEqualTo(original.getId());
        assertThat(result.getOwnerId()).isEqualTo(original.getOwnerId());
        assertThat(result.getInviteCode()).isEqualTo(original.getInviteCode());
        assertThat(result.getName()).isEqualTo(original.getName());
        assertThat(result.getDescription()).isNull();
    }
}
