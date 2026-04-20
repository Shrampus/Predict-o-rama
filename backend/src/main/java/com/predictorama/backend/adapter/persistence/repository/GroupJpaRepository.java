package com.predictorama.backend.adapter.persistence.repository;

import com.predictorama.backend.adapter.persistence.entity.GroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupJpaRepository extends JpaRepository<GroupEntity, UUID> {

    List<GroupEntity> findByOwnerId(UUID ownerId);

    Optional<GroupEntity> findByInviteCode(UUID inviteCode);
}
