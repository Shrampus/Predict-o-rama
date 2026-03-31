package com.predictorama.backend.domain.port.persistence;

import com.predictorama.backend.domain.entity.Group;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupRepositoryPort {

    Group save(Group group);

    Optional<Group> findById(UUID id);

    Optional<Group> findByInviteCode(UUID inviteCode);

    List<Group> findByOwnerId(UUID ownerId);
}
