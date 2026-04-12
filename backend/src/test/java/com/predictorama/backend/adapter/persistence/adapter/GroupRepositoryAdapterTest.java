package com.predictorama.backend.adapter.persistence.adapter;

import com.predictorama.backend.adapter.persistence.entity.GroupEntity;
import com.predictorama.backend.adapter.persistence.repository.GroupJpaRepository;
import com.predictorama.backend.domain.entity.Group;
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
class GroupRepositoryAdapterTest {

    @Mock
    private GroupJpaRepository jpaRepository;

    @InjectMocks
    private GroupRepositoryAdapter adapter;

    @Test
    void save_mapsToEntityPersistsAndReturnsDomain() {
        UUID id = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID inviteCode = UUID.randomUUID();
        Group group = Group.builder().id(id).ownerId(ownerId).inviteCode(inviteCode).name("Legends").description("Desc").build();
        GroupEntity entity = GroupEntity.builder().id(id).ownerId(ownerId).inviteCode(inviteCode).name("Legends").description("Desc").build();
        when(jpaRepository.save(any(GroupEntity.class))).thenReturn(entity);

        Group result = adapter.save(group);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getName()).isEqualTo("Legends");
        verify(jpaRepository).save(any(GroupEntity.class));
    }

    @Test
    void findById_returnsMappedDomainWhenFound() {
        UUID id = UUID.randomUUID();
        GroupEntity entity = GroupEntity.builder().id(id).ownerId(UUID.randomUUID()).inviteCode(UUID.randomUUID()).name("Legends").build();
        when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));

        Optional<Group> result = adapter.findById(id);

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
    void findByInviteCode_returnsMappedWhenFound() {
        UUID inviteCode = UUID.randomUUID();
        GroupEntity entity = GroupEntity.builder().id(UUID.randomUUID()).ownerId(UUID.randomUUID()).inviteCode(inviteCode).name("Legends").build();
        when(jpaRepository.findByInviteCode(inviteCode)).thenReturn(Optional.of(entity));

        Optional<Group> result = adapter.findByInviteCode(inviteCode);

        assertThat(result).isPresent();
        assertThat(result.get().getInviteCode()).isEqualTo(inviteCode);
    }

    @Test
    void findByInviteCode_returnsEmptyWhenNotFound() {
        UUID inviteCode = UUID.randomUUID();
        when(jpaRepository.findByInviteCode(inviteCode)).thenReturn(Optional.empty());

        assertThat(adapter.findByInviteCode(inviteCode)).isEmpty();
    }

    @Test
    void findByOwnerId_returnsMappedList() {
        UUID ownerId = UUID.randomUUID();
        GroupEntity entity1 = GroupEntity.builder().id(UUID.randomUUID()).ownerId(ownerId).inviteCode(UUID.randomUUID()).name("A").build();
        GroupEntity entity2 = GroupEntity.builder().id(UUID.randomUUID()).ownerId(ownerId).inviteCode(UUID.randomUUID()).name("B").build();
        when(jpaRepository.findByOwnerId(ownerId)).thenReturn(List.of(entity1, entity2));

        List<Group> result = adapter.findByOwnerId(ownerId);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Group::getOwnerId).containsOnly(ownerId);
    }

    @Test
    void findByOwnerId_returnsEmptyListWhenNoneFound() {
        UUID ownerId = UUID.randomUUID();
        when(jpaRepository.findByOwnerId(ownerId)).thenReturn(List.of());

        assertThat(adapter.findByOwnerId(ownerId)).isEmpty();
    }
}
