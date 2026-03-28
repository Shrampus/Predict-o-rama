package com.predictorama.backend.domain.port;

import com.predictorama.backend.domain.entity.GroupMember;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupMemberRepositoryPort {

    GroupMember save(GroupMember groupMember);

    Optional<GroupMember> findById(UUID id);

    List<GroupMember> findByGroupId(UUID groupId);

    List<GroupMember> findByUserId(UUID userId);

    Optional<GroupMember> findByGroupIdAndUserId(UUID groupId, UUID userId);

    void deleteByGroupIdAndUserId(UUID groupId, UUID userId);
}
