package com.predictorama.backend.domain.service;

import com.predictorama.backend.domain.entity.Group;
import com.predictorama.backend.domain.entity.GroupMember;
import com.predictorama.backend.domain.entity.Role;
import com.predictorama.backend.domain.entity.aggregate.UserGroups;
import com.predictorama.backend.domain.exception.AlreadyMemberException;
import com.predictorama.backend.domain.port.persistence.GroupMemberRepositoryPort;
import com.predictorama.backend.domain.port.persistence.GroupRepositoryPort;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class GroupService {

    private final GroupRepositoryPort groupRepository;
    private final GroupMemberRepositoryPort groupMemberRepository;

    public Group createGroup(UUID ownerId, String name, String description) {
        Group group = Group.builder()
                .id(UUID.randomUUID())
                .ownerId(ownerId)
                .name(name)
                .description(description)
                .inviteCode(UUID.randomUUID())
                .build();
        Group saved = groupRepository.save(group);

        GroupMember ownerMembership = GroupMember.builder()
                .id(UUID.randomUUID())
                .groupId(saved.getId())
                .userId(ownerId)
                .status(GroupMember.MemberStatus.ACTIVE)
                .memberRole(Role.ADMIN)
                .build();
        groupMemberRepository.save(ownerMembership);

        return saved;
    }

    public Optional<GroupMember> joinGroup(UUID userId, UUID inviteCode) {
        return groupRepository.findByInviteCode(inviteCode)
                .map(group -> {
                    if (groupMemberRepository.findByGroupIdAndUserId(group.getId(), userId).isPresent()) {
                        throw new AlreadyMemberException(userId, group.getId());
                    }
                    GroupMember membership = GroupMember.builder()
                            .id(UUID.randomUUID())
                            .groupId(group.getId())
                            .userId(userId)
                            .status(GroupMember.MemberStatus.ACTIVE)
                            .memberRole(Role.USER)
                            .build();
                    return groupMemberRepository.save(membership);
                });
    }

    public void leaveGroup(UUID userId, UUID groupId) {
        groupMemberRepository.deleteByGroupIdAndUserId(groupId, userId);
    }

    public List<GroupMember> getGroupMembers(UUID groupId) {
        return groupMemberRepository.findByGroupId(groupId);
    }

    public List<UserGroups> getUserGroups(UUID userId) {
        return groupMemberRepository.findByUserId(userId).stream()
                .map(membership -> groupRepository.findById(membership.getGroupId())
                        .map(group -> new UserGroups(group, membership))
                        .orElseThrow(() -> new IllegalStateException("Group not found for membership: " + membership.getId())))
                .toList();
    }
}
