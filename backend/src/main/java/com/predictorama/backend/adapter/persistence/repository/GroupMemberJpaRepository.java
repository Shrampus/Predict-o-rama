package com.predictorama.backend.adapter.persistence.repository;

import com.predictorama.backend.adapter.persistence.entity.GroupMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupMemberJpaRepository extends JpaRepository<GroupMemberEntity, UUID> {

    List<GroupMemberEntity> findByGroupId(UUID groupId);

    List<GroupMemberEntity> findByUserId(UUID userId);

    Optional<GroupMemberEntity> findByGroupIdAndUserId(UUID groupId, UUID userId);

    @Transactional
    void deleteByGroupIdAndUserId(UUID groupId, UUID userId);
}
