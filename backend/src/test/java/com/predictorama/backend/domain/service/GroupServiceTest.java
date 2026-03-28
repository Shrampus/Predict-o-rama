package com.predictorama.backend.domain.service;

import com.predictorama.backend.domain.entity.Group;
import com.predictorama.backend.domain.entity.GroupMember;
import com.predictorama.backend.domain.entity.Role;
import com.predictorama.backend.domain.port.GroupMemberRepositoryPort;
import com.predictorama.backend.domain.port.GroupRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class GroupServiceTest {

    private GroupService groupService;
    private InMemoryGroupRepository groupRepository;
    private InMemoryGroupMemberRepository groupMemberRepository;

    @BeforeEach
    void setUp() {
        groupRepository = new InMemoryGroupRepository();
        groupMemberRepository = new InMemoryGroupMemberRepository();
        groupService = new GroupService(groupRepository, groupMemberRepository);
    }

    @Test
    void createGroup_savesGroupWithGeneratedIdAndInviteCode() {
        UUID ownerId = UUID.randomUUID();

        Group group = groupService.createGroup(ownerId, "Legends", "Best group");

        assertThat(group.getId()).isNotNull();
        assertThat(group.getInviteCode()).isNotNull();
        assertThat(group.getName()).isEqualTo("Legends");
        assertThat(group.getDescription()).isEqualTo("Best group");
        assertThat(group.getOwnerId()).isEqualTo(ownerId);
    }

    @Test
    void createGroup_persistsGroupToRepository() {
        UUID ownerId = UUID.randomUUID();

        Group group = groupService.createGroup(ownerId, "Legends", null);

        assertThat(groupRepository.findById(group.getId())).isPresent();
    }

    @Test
    void createGroup_createsOwnerMembershipWithAdminRole() {
        UUID ownerId = UUID.randomUUID();

        Group group = groupService.createGroup(ownerId, "Legends", null);

        List<GroupMember> members = groupMemberRepository.findByGroupId(group.getId());
        assertThat(members).hasSize(1);

        GroupMember ownerMember = members.get(0);
        assertThat(ownerMember.getUserId()).isEqualTo(ownerId);
        assertThat(ownerMember.getMemberRole()).isEqualTo(Role.ADMIN);
        assertThat(ownerMember.getStatus()).isEqualTo(GroupMember.MemberStatus.ACTIVE);
    }

    @Test
    void joinGroup_withValidInviteCode_returnsActiveMembership() {
        UUID ownerId = UUID.randomUUID();
        Group group = groupService.createGroup(ownerId, "Legends", null);
        UUID joiningUserId = UUID.randomUUID();

        Optional<GroupMember> result = groupService.joinGroup(joiningUserId, group.getInviteCode());

        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(joiningUserId);
        assertThat(result.get().getGroupId()).isEqualTo(group.getId());
        assertThat(result.get().getMemberRole()).isEqualTo(Role.USER);
        assertThat(result.get().getStatus()).isEqualTo(GroupMember.MemberStatus.ACTIVE);
    }

    @Test
    void joinGroup_withInvalidInviteCode_returnsEmpty() {
        UUID nonExistentInviteCode = UUID.randomUUID();

        Optional<GroupMember> result = groupService.joinGroup(UUID.randomUUID(), nonExistentInviteCode);

        assertThat(result).isEmpty();
    }

    @Test
    void joinGroup_persistsMembership() {
        UUID ownerId = UUID.randomUUID();
        Group group = groupService.createGroup(ownerId, "Legends", null);
        UUID joiningUserId = UUID.randomUUID();

        groupService.joinGroup(joiningUserId, group.getInviteCode());

        List<GroupMember> members = groupMemberRepository.findByGroupId(group.getId());
        assertThat(members).hasSize(2); // owner + new member
    }

    @Test
    void getGroupMembers_returnsAllMembersForGroup() {
        UUID ownerId = UUID.randomUUID();
        Group group = groupService.createGroup(ownerId, "Legends", null);
        groupService.joinGroup(UUID.randomUUID(), group.getInviteCode());

        List<GroupMember> members = groupService.getGroupMembers(group.getId());

        assertThat(members).hasSize(2);
    }

    @Test
    void getGroupMembers_returnsEmptyForUnknownGroup() {
        List<GroupMember> members = groupService.getGroupMembers(UUID.randomUUID());

        assertThat(members).isEmpty();
    }

    // --- In-memory stubs ---

    static class InMemoryGroupRepository implements GroupRepositoryPort {
        private final Map<UUID, Group> store = new HashMap<>();

        @Override
        public Group save(Group group) {
            store.put(group.getId(), group);
            return group;
        }

        @Override
        public Optional<Group> findById(UUID id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public Optional<Group> findByInviteCode(UUID inviteCode) {
            return store.values().stream().filter(g -> g.getInviteCode().equals(inviteCode)).findFirst();
        }

        @Override
        public List<Group> findByOwnerId(UUID ownerId) {
            return store.values().stream().filter(g -> g.getOwnerId().equals(ownerId)).toList();
        }
    }

    static class InMemoryGroupMemberRepository implements GroupMemberRepositoryPort {
        private final Map<UUID, GroupMember> store = new HashMap<>();

        @Override
        public GroupMember save(GroupMember member) {
            store.put(member.getId(), member);
            return member;
        }

        @Override
        public Optional<GroupMember> findById(UUID id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public List<GroupMember> findByGroupId(UUID groupId) {
            return store.values().stream().filter(m -> m.getGroupId().equals(groupId)).toList();
        }

        @Override
        public List<GroupMember> findByUserId(UUID userId) {
            return store.values().stream().filter(m -> m.getUserId().equals(userId)).toList();
        }

        @Override
        public Optional<GroupMember> findByGroupIdAndUserId(UUID groupId, UUID userId) {
            return store.values().stream()
                    .filter(m -> m.getGroupId().equals(groupId) && m.getUserId().equals(userId))
                    .findFirst();
        }

        @Override
        public void deleteByGroupIdAndUserId(UUID groupId, UUID userId) {
            store.values().removeIf(m -> m.getGroupId().equals(groupId) && m.getUserId().equals(userId));
        }
    }
}
