package com.predictorama.backend.adapter.persistence.adapter;

import com.predictorama.backend.adapter.persistence.mapper.GroupMemberMapper;
import com.predictorama.backend.adapter.persistence.repository.GroupMemberJpaRepository;
import com.predictorama.backend.domain.entity.GroupMember;
import com.predictorama.backend.domain.port.persistence.GroupMemberRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class GroupMemberRepositoryAdapter implements GroupMemberRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(GroupMemberRepositoryAdapter.class);

    private final GroupMemberJpaRepository jpaRepository;

    @Override
    public GroupMember save(GroupMember groupMember) {
        log.debug("Saving group member - id={}, groupId={}, userId={}", groupMember.getId(), groupMember.getGroupId(), groupMember.getUserId());
        GroupMember saved = GroupMemberMapper.toDomain(jpaRepository.save(GroupMemberMapper.toEntity(groupMember)));
        log.debug("Group member saved - id={}", saved.getId());
        return saved;
    }

    @Override
    public Optional<GroupMember> findById(UUID id) {
        return jpaRepository.findById(id).map(GroupMemberMapper::toDomain);
    }

    @Override
    public List<GroupMember> findByGroupId(UUID groupId) {
        return jpaRepository.findByGroupId(groupId).stream().map(GroupMemberMapper::toDomain).toList();
    }

    @Override
    public List<GroupMember> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).stream().map(GroupMemberMapper::toDomain).toList();
    }

    @Override
    public Optional<GroupMember> findByGroupIdAndUserId(UUID groupId, UUID userId) {
        return jpaRepository.findByGroupIdAndUserId(groupId, userId).map(GroupMemberMapper::toDomain);
    }

    @Override
    public void deleteByGroupIdAndUserId(UUID groupId, UUID userId) {
        log.debug("Deleting group member - groupId={}, userId={}", groupId, userId);
        jpaRepository.deleteByGroupIdAndUserId(groupId, userId);
    }
}
