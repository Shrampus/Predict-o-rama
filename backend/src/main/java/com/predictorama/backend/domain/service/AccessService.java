package com.predictorama.backend.domain.service;

import com.predictorama.backend.domain.entity.Group;
import com.predictorama.backend.domain.entity.GroupMember;
import com.predictorama.backend.domain.entity.Role;
import com.predictorama.backend.domain.exception.GroupAccessDeniedException;
import com.predictorama.backend.domain.exception.GroupNotFoundException;
import com.predictorama.backend.domain.port.persistence.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccessService {
    private final GroupRepositoryPort groupRepository;
    private final GroupMemberRepositoryPort groupMemberRepository;

    public GroupMember requireActiveMembership(UUID userId, UUID groupId) {
        requireExistingGroup(groupId);
        return groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .filter(membership -> membership.getStatus() == GroupMember.MemberStatus.ACTIVE)
                .orElseThrow(() -> new GroupAccessDeniedException(userId, groupId));
    }

    public void requireAdminMembership(UUID userId, UUID groupId) {
        GroupMember membership = requireActiveMembership(userId, groupId);
        if (membership.getMemberRole() != Role.ADMIN) {
            throw new GroupAccessDeniedException(userId, groupId);
        }
    }

    public Group requireExistingGroup(UUID groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));
    }
}
