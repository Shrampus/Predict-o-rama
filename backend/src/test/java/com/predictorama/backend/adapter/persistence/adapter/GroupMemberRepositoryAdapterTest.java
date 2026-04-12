package com.predictorama.backend.adapter.persistence.adapter;

import com.predictorama.backend.adapter.persistence.entity.GroupMemberEntity;
import com.predictorama.backend.adapter.persistence.repository.GroupMemberJpaRepository;
import com.predictorama.backend.domain.entity.GroupMember;
import com.predictorama.backend.domain.entity.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupMemberRepositoryAdapterTest {

    @Mock
    private GroupMemberJpaRepository jpaRepository;

    @InjectMocks
    private GroupMemberRepositoryAdapter adapter;

    @Test
    void save_mapsToEntityPersistsAndReturnsDomain() {
        UUID id = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        GroupMember member = GroupMember.builder().id(id).groupId(groupId).userId(userId).memberRole(Role.USER).status(GroupMember.MemberStatus.ACTIVE).build();
        GroupMemberEntity entity = GroupMemberEntity.builder().id(id).groupId(groupId).userId(userId).memberRole(Role.USER).status(GroupMember.MemberStatus.ACTIVE).build();
        when(jpaRepository.save(any(GroupMemberEntity.class))).thenReturn(entity);

        GroupMember result = adapter.save(member);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getMemberRole()).isEqualTo(Role.USER);
        verify(jpaRepository).save(any(GroupMemberEntity.class));
    }

    @Test
    void findById_returnsMappedWhenFound() {
        UUID id = UUID.randomUUID();
        GroupMemberEntity entity = GroupMemberEntity.builder().id(id).groupId(UUID.randomUUID()).userId(UUID.randomUUID()).memberRole(Role.USER).status(GroupMember.MemberStatus.ACTIVE).build();
        when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));

        Optional<GroupMember> result = adapter.findById(id);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(id);
    }

    @Test
    void findById_returnsEmptyWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(jpaRepository.findById(id)).thenReturn(Optional.empty());

        assertThat(adapter.findById(id)).isEmpty();
    }

    @Test
    void findByGroupId_returnsMappedList() {
        UUID groupId = UUID.randomUUID();
        GroupMemberEntity e1 = GroupMemberEntity.builder().id(UUID.randomUUID()).groupId(groupId).userId(UUID.randomUUID()).memberRole(Role.ADMIN).status(GroupMember.MemberStatus.ACTIVE).build();
        GroupMemberEntity e2 = GroupMemberEntity.builder().id(UUID.randomUUID()).groupId(groupId).userId(UUID.randomUUID()).memberRole(Role.USER).status(GroupMember.MemberStatus.ACTIVE).build();
        when(jpaRepository.findByGroupId(groupId)).thenReturn(List.of(e1, e2));

        List<GroupMember> result = adapter.findByGroupId(groupId);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(GroupMember::getGroupId).containsOnly(groupId);
    }

    @Test
    void findByUserId_returnsMappedList() {
        UUID userId = UUID.randomUUID();
        GroupMemberEntity entity = GroupMemberEntity.builder().id(UUID.randomUUID()).groupId(UUID.randomUUID()).userId(userId).memberRole(Role.USER).status(GroupMember.MemberStatus.ACTIVE).build();
        when(jpaRepository.findByUserId(userId)).thenReturn(List.of(entity));

        List<GroupMember> result = adapter.findByUserId(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(userId);
    }

    @Test
    void findByGroupIdAndUserId_returnsMappedWhenFound() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        GroupMemberEntity entity = GroupMemberEntity.builder().id(UUID.randomUUID()).groupId(groupId).userId(userId).memberRole(Role.USER).status(GroupMember.MemberStatus.ACTIVE).build();
        when(jpaRepository.findByGroupIdAndUserId(groupId, userId)).thenReturn(Optional.of(entity));

        Optional<GroupMember> result = adapter.findByGroupIdAndUserId(groupId, userId);

        assertThat(result).isPresent();
        assertThat(result.get().getGroupId()).isEqualTo(groupId);
        assertThat(result.get().getUserId()).isEqualTo(userId);
    }

    @Test
    void findByGroupIdAndUserId_returnsEmptyWhenNotFound() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(jpaRepository.findByGroupIdAndUserId(groupId, userId)).thenReturn(Optional.empty());

        assertThat(adapter.findByGroupIdAndUserId(groupId, userId)).isEmpty();
    }

    @Test
    void deleteByGroupIdAndUserId_delegatesToJpa() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        adapter.deleteByGroupIdAndUserId(groupId, userId);

        verify(jpaRepository).deleteByGroupIdAndUserId(groupId, userId);
    }
}
