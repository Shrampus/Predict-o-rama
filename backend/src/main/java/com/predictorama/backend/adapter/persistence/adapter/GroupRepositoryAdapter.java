package com.predictorama.backend.adapter.persistence.adapter;

import com.predictorama.backend.adapter.persistence.mapper.GroupMapper;
import com.predictorama.backend.adapter.persistence.repository.GroupJpaRepository;
import com.predictorama.backend.domain.entity.Group;
import com.predictorama.backend.domain.port.GroupRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class GroupRepositoryAdapter implements GroupRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(GroupRepositoryAdapter.class);

    private final GroupJpaRepository jpaRepository;

    @Override
    public Group save(Group group) {
        log.debug("Saving group - id={}, name={}", group.getId(), group.getName());
        Group saved = GroupMapper.toDomain(jpaRepository.save(GroupMapper.toEntity(group)));
        log.debug("Group saved - id={}", saved.getId());
        return saved;
    }

    @Override
    public Optional<Group> findById(UUID id) {
        return jpaRepository.findById(id).map(GroupMapper::toDomain);
    }

    @Override
    public Optional<Group> findByInviteCode(UUID inviteCode) {
        return jpaRepository.findByInviteCode(inviteCode).map(GroupMapper::toDomain);
    }

    @Override
    public List<Group> findByOwnerId(UUID ownerId) {
        return jpaRepository.findByOwnerId(ownerId).stream().map(GroupMapper::toDomain).toList();
    }
}
