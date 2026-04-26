package com.predictorama.backend.domain.service;

import com.predictorama.backend.domain.entity.Group;
import com.predictorama.backend.domain.entity.GroupMember;
import com.predictorama.backend.domain.entity.Match;
import com.predictorama.backend.domain.entity.Prediction;
import com.predictorama.backend.domain.entity.Role;
import com.predictorama.backend.domain.entity.Tournament;
import com.predictorama.backend.domain.entity.aggregate.GroupDetailsView;
import com.predictorama.backend.domain.entity.aggregate.GroupLeaderboardView;
import com.predictorama.backend.domain.entity.aggregate.GroupMemberView;
import com.predictorama.backend.domain.entity.aggregate.UserGroups;
import com.predictorama.backend.domain.exception.AlreadyMemberException;
import com.predictorama.backend.domain.exception.GroupAccessDeniedException;
import com.predictorama.backend.domain.exception.GroupMemberNotFoundException;
import com.predictorama.backend.domain.exception.GroupNotFoundException;
import com.predictorama.backend.domain.exception.TournamentAlreadyLinkedException;
import com.predictorama.backend.domain.exception.TournamentNotLinkedException;
import com.predictorama.backend.domain.exception.TournamentNotFoundException;
import com.predictorama.backend.domain.exception.UserNotFoundException;
import com.predictorama.backend.domain.port.persistence.GroupTournamentRepositoryPort;
import com.predictorama.backend.domain.port.persistence.GroupMemberRepositoryPort;
import com.predictorama.backend.domain.port.persistence.GroupRepositoryPort;
import com.predictorama.backend.domain.port.persistence.MatchRepositoryPort;
import com.predictorama.backend.domain.port.persistence.PredictionRepositoryPort;
import com.predictorama.backend.domain.port.persistence.TournamentRepositoryPort;
import com.predictorama.backend.domain.port.persistence.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GroupService {

    private final GroupRepositoryPort groupRepository;
    private final GroupMemberRepositoryPort groupMemberRepository;
    private final UserRepositoryPort userRepository;
    private final TournamentRepositoryPort tournamentRepository;
    private final GroupTournamentRepositoryPort groupTournamentRepository;
    private final MatchRepositoryPort matchRepository;
    private final PredictionRepositoryPort predictionRepository;

    @Transactional
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

    @Transactional
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

    @Transactional
    public void leaveGroup(UUID userId, UUID groupId) {
        groupMemberRepository.deleteByGroupIdAndUserId(groupId, userId);
    }

    public GroupDetailsView getGroupDetails(UUID userId, UUID groupId) {
        Group group = requireExistingGroup(groupId);
        GroupMember membership = requireActiveMembership(userId, groupId);
        return new GroupDetailsView(group, membership.getMemberRole());
    }

    public List<GroupMemberView> getGroupMembers(UUID userId, UUID groupId) {
        requireActiveMembership(userId, groupId);
        return groupMemberRepository.findByGroupIdWithUsernames(groupId);
    }

    @Transactional
    public GroupMember addMemberByEmail(UUID adminUserId, UUID groupId, String email) {
        requireAdminMembership(adminUserId, groupId);
        UUID userIdToAdd = userRepository.findByEmail(email)
                .map(user -> user.getId())
                .orElseThrow(() -> new UserNotFoundException(email));

        if (groupMemberRepository.findByGroupIdAndUserId(groupId, userIdToAdd).isPresent()) {
            throw new AlreadyMemberException(userIdToAdd, groupId);
        }

        GroupMember newMembership = GroupMember.builder()
                .id(UUID.randomUUID())
                .groupId(groupId)
                .userId(userIdToAdd)
                .status(GroupMember.MemberStatus.ACTIVE)
                .memberRole(Role.USER)
                .build();
        return groupMemberRepository.save(newMembership);
    }

    @Transactional
    public void removeMember(UUID adminUserId, UUID groupId, UUID memberUserId) {
        requireAdminMembership(adminUserId, groupId);

        if (adminUserId.equals(memberUserId)) {
            throw new IllegalArgumentException("Admins cannot remove themselves.");
        }

        GroupMember membershipToRemove = groupMemberRepository.findByGroupIdAndUserId(groupId, memberUserId)
                .orElseThrow(() -> new GroupMemberNotFoundException(groupId, memberUserId));

        if (membershipToRemove.getStatus() != GroupMember.MemberStatus.ACTIVE) {
            throw new IllegalArgumentException("Only active group members can be removed.");
        }

        if (membershipToRemove.getMemberRole() == Role.ADMIN) {
            throw new IllegalArgumentException("Admins cannot remove other admins.");
        }

        groupMemberRepository.deleteByGroupIdAndUserId(groupId, memberUserId);
    }

    public List<Tournament> getGroupTournaments(UUID userId, UUID groupId) {
        requireActiveMembership(userId, groupId);
        return groupTournamentRepository.findTournamentIdsByGroupId(groupId).stream()
                .map(tournamentId -> tournamentRepository.findById(tournamentId)
                        .orElseThrow(() -> new TournamentNotFoundException(tournamentId)))
                .toList();
    }

    public List<GroupLeaderboardView> getGroupLeaderboards(UUID userId, UUID groupId) {
        requireActiveMembership(userId, groupId);

        List<GroupMemberView> activeMembers = groupMemberRepository.findByGroupIdWithUsernames(groupId).stream()
                .filter(memberView -> memberView.member().getStatus() == GroupMember.MemberStatus.ACTIVE)
                .toList();
        List<Prediction> groupPredictions = predictionRepository.findByGroupId(groupId);

        return getGroupTournaments(userId, groupId).stream()
                .map(tournament -> buildLeaderboard(tournament, activeMembers, groupPredictions))
                .toList();
    }

    @Transactional
    public void addTournamentToGroup(UUID adminUserId, UUID groupId, UUID tournamentId) {
        requireAdminMembership(adminUserId, groupId);
        tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(tournamentId));

        if (groupTournamentRepository.existsByGroupIdAndTournamentId(groupId, tournamentId)) {
            throw new TournamentAlreadyLinkedException(groupId, tournamentId);
        }

        groupTournamentRepository.save(groupId, tournamentId);
    }

    @Transactional
    public void removeTournamentFromGroup(UUID adminUserId, UUID groupId, UUID tournamentId) {
        requireAdminMembership(adminUserId, groupId);
        if (!groupTournamentRepository.existsByGroupIdAndTournamentId(groupId, tournamentId)) {
            throw new TournamentNotLinkedException(groupId, tournamentId);
        }

        groupTournamentRepository.delete(groupId, tournamentId);
    }

    public Optional<GroupPreviewView> getGroupPreview(UUID inviteCode) {
        return groupRepository.findByInviteCode(inviteCode)
                .map(group -> {
                    String adminName = userRepository.findById(group.getOwnerId())
                            .map(user -> user.getUsername())
                            .orElse(group.getOwnerId().toString());
                    List<UUID> tournamentIds = groupTournamentRepository.findTournamentIdsByGroupId(group.getId());
                    List<String> tournamentNames = tournamentRepository.findAllById(new HashSet<>(tournamentIds)).stream()
                            .map(Tournament::getName)
                            .toList();
                    return new GroupPreviewView(group.getName(), group.getDescription(), adminName, tournamentNames);
                });
    }

    public record GroupPreviewView(String name, String description, String adminName, List<String> tournamentNames) {}

    public List<UserGroups> getUserGroups(UUID userId) {
        return groupMemberRepository.findByUserId(userId).stream()
                .map(membership -> groupRepository.findById(membership.getGroupId())
                        .map(group -> new UserGroups(group, membership))
                        .orElseThrow(() -> new IllegalStateException("Group not found for membership: " + membership.getId())))
                .toList();
    }

    private GroupLeaderboardView buildLeaderboard(
            Tournament tournament,
            List<GroupMemberView> activeMembers,
            List<Prediction> groupPredictions
    ) {
        Map<UUID, Match> tournamentMatchesById = matchRepository.findByTournamentId(tournament.getId()).stream()
                .collect(Collectors.toMap(Match::getId, Function.identity()));

        Map<UUID, List<Prediction>> predictionsByUserId = groupPredictions.stream()
                .filter(prediction -> tournamentMatchesById.containsKey(prediction.getMatchId()))
                .collect(Collectors.groupingBy(Prediction::getUserId));

        List<GroupLeaderboardView.Entry> entries = activeMembers.stream()
                .map(memberView -> toLeaderboardEntry(
                        memberView,
                        predictionsByUserId.getOrDefault(memberView.member().getUserId(), List.of())
                ))
                .sorted((left, right) -> {
                    int scoreComparison = Integer.compare(right.totalScore(), left.totalScore());
                    if (scoreComparison != 0) {
                        return scoreComparison;
                    }
                    return left.username().compareToIgnoreCase(right.username());
                })
                .toList();

        return new GroupLeaderboardView(tournament, entries);
    }

    private GroupLeaderboardView.Entry toLeaderboardEntry(GroupMemberView memberView, List<Prediction> predictions) {
        int totalScore = predictions.stream()
                .map(Prediction::getResult)
                .filter(result -> result != null)
                .mapToInt(Integer::intValue)
                .sum();
        int scoredPredictions = (int) predictions.stream()
                .filter(prediction -> prediction.getResult() != null)
                .count();

        return new GroupLeaderboardView.Entry(
                memberView.member().getUserId(),
                memberView.username(),
                totalScore,
                scoredPredictions,
                predictions.size()
        );
    }

    private GroupMember requireActiveMembership(UUID userId, UUID groupId) {
        requireExistingGroup(groupId);
        return groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .filter(membership -> membership.getStatus() == GroupMember.MemberStatus.ACTIVE)
                .orElseThrow(() -> new GroupAccessDeniedException(userId, groupId));
    }

    private void requireAdminMembership(UUID userId, UUID groupId) {
        GroupMember membership = requireActiveMembership(userId, groupId);
        if (membership.getMemberRole() != Role.ADMIN) {
            throw new GroupAccessDeniedException(userId, groupId);
        }
    }

    private Group requireExistingGroup(UUID groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));
    }
}
