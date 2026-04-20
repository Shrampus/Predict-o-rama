package com.predictorama.backend.adapter.persistence.mapper;

import com.predictorama.backend.adapter.persistence.entity.GroupMemberEntity;
import com.predictorama.backend.domain.entity.GroupMember;
import com.predictorama.backend.domain.entity.Role;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GroupMemberMapperTest {

    @Test
    void toEntity_mapsAllFields() {
        GroupMember member = GroupMember.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .groupId(UUID.randomUUID())
                .memberRole(Role.ADMIN)
                .status(GroupMember.MemberStatus.ACTIVE)
                .build();

        GroupMemberEntity entity = GroupMemberMapper.toEntity(member);

        assertThat(entity.getId()).isEqualTo(member.getId());
        assertThat(entity.getUserId()).isEqualTo(member.getUserId());
        assertThat(entity.getGroupId()).isEqualTo(member.getGroupId());
        assertThat(entity.getMemberRole()).isEqualTo(member.getMemberRole());
        assertThat(entity.getStatus()).isEqualTo(member.getStatus());
    }

    @Test
    void toDomain_mapsAllFields() {
        GroupMemberEntity entity = GroupMemberEntity.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .groupId(UUID.randomUUID())
                .memberRole(Role.USER)
                .status(GroupMember.MemberStatus.INACTIVE)
                .build();

        GroupMember member = GroupMemberMapper.toDomain(entity);

        assertThat(member.getId()).isEqualTo(entity.getId());
        assertThat(member.getUserId()).isEqualTo(entity.getUserId());
        assertThat(member.getGroupId()).isEqualTo(entity.getGroupId());
        assertThat(member.getMemberRole()).isEqualTo(entity.getMemberRole());
        assertThat(member.getStatus()).isEqualTo(entity.getStatus());
    }

    @Test
    void roundTrip_domainToEntityToDomain_preservesAllFields() {
        GroupMember original = GroupMember.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .groupId(UUID.randomUUID())
                .memberRole(Role.USER)
                .status(GroupMember.MemberStatus.ACTIVE)
                .build();

        GroupMember result = GroupMemberMapper.toDomain(GroupMemberMapper.toEntity(original));

        assertThat(result.getId()).isEqualTo(original.getId());
        assertThat(result.getUserId()).isEqualTo(original.getUserId());
        assertThat(result.getGroupId()).isEqualTo(original.getGroupId());
        assertThat(result.getMemberRole()).isEqualTo(original.getMemberRole());
        assertThat(result.getStatus()).isEqualTo(original.getStatus());
    }
}
