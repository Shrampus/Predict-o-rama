package com.predictorama.backend.domain.service;

import com.predictorama.backend.domain.entity.*;
import com.predictorama.backend.domain.port.persistence.*;
import com.predictorama.backend.domain.service.result.UpcomingMatchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class UserUpcomingMatchQueryServiceTest {

    private UserUpcomingMatchQueryService service;
    private InMemoryGroupMemberRepository groupMemberRepository;
    private InMemoryGroupRepository groupRepository;
    private InMemoryGroupTournamentRepository groupTournamentRepository;
    private InMemoryMatchRepository matchRepository;
    private InMemoryTournamentRepository tournamentRepository;

    @BeforeEach
    void setUp() {
        groupMemberRepository = new InMemoryGroupMemberRepository();
        groupRepository = new InMemoryGroupRepository();
        groupTournamentRepository = new InMemoryGroupTournamentRepository();
        matchRepository = new InMemoryMatchRepository();
        tournamentRepository = new InMemoryTournamentRepository();
        service = new UserUpcomingMatchQueryService(
                groupMemberRepository,
                groupRepository,
                groupTournamentRepository,
                matchRepository,
                tournamentRepository,
                new CompetitionCatalog()
        );
    }

    @Test
    void getUpcomingMatches_attachesOnlyGroupsWhoseTournamentMatchesTheMatch() {
        UUID userId = UUID.randomUUID();
        UUID tournamentId = UUID.randomUUID();
        Group groupA = group("Group A");
        Group groupB = group("Group B");
        groupRepository.save(groupA);
        groupRepository.save(groupB);
        groupMemberRepository.add(member(userId, groupA.getId()));
        groupMemberRepository.add(member(userId, groupB.getId()));
        groupTournamentRepository.save(groupA.getId(), tournamentId);
        // groupB has no tournament linked

        matchRepository.add(matchWithTournament(tournamentId, Instant.now().plus(5, ChronoUnit.DAYS)));

        List<UpcomingMatchResult> results = service.getUpcomingMatches(userId);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getUserGroups()).hasSize(1);
        assertThat(results.get(0).getUserGroups().get(0).getName()).isEqualTo("Group A");
    }

    @Test
    void getUpcomingMatches_returnsEmptyGroupsWhenUserHasNoGroups() {
        UUID userId = UUID.randomUUID();
        matchRepository.add(matchWithKickoff(Instant.now().plus(5, ChronoUnit.DAYS)));

        List<UpcomingMatchResult> results = service.getUpcomingMatches(userId);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getUserGroups()).isEmpty();
    }

    @Test
    void getUpcomingMatches_matchBelongingToMultipleGroupsGetsAllRelevantGroups() {
        UUID userId = UUID.randomUUID();
        UUID tournamentId = UUID.randomUUID();

        Group groupA = group("Group A");
        Group groupB = group("Group B");
        groupRepository.save(groupA);
        groupRepository.save(groupB);
        groupMemberRepository.add(member(userId, groupA.getId()));
        groupMemberRepository.add(member(userId, groupB.getId()));
        groupTournamentRepository.save(groupA.getId(), tournamentId);
        groupTournamentRepository.save(groupB.getId(), tournamentId);

        matchRepository.add(matchWithTournament(tournamentId, Instant.now().plus(5, ChronoUnit.DAYS)));

        List<UpcomingMatchResult> results = service.getUpcomingMatches(userId);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getUserGroups()).hasSize(2);
    }

    // --- Helpers ---

    private Match matchWithKickoff(Instant kickoff) {
        return matchWithTournament(UUID.randomUUID(), kickoff);
    }

    private Match matchWithTournament(UUID tournamentId, Instant kickoff) {
        Team team = Team.builder().id(UUID.randomUUID()).name("Team").imageUrl("").build();
        return Match.builder()
                .id(UUID.randomUUID())
                .tournamentId(tournamentId)
                .homeTeam(team)
                .awayTeam(team)
                .matchStatus(Match.MatchStatus.SCHEDULED)
                .kickoffTime(kickoff)
                .build();
    }

    private Group group(String name) {
        return Group.builder()
                .id(UUID.randomUUID())
                .ownerId(UUID.randomUUID())
                .inviteCode(UUID.randomUUID())
                .name(name)
                .build();
    }

    private GroupMember member(UUID userId, UUID groupId) {
        return GroupMember.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .groupId(groupId)
                .memberRole(Role.USER)
                .status(GroupMember.MemberStatus.ACTIVE)
                .build();
    }

    // --- In-memory stubs ---

    static class InMemoryMatchRepository implements MatchRepositoryPort {
        private final List<Match> store = new ArrayList<>();

        void add(Match match) { store.add(match); }

        @Override
        public Match save(Match match) { store.add(match); return match; }

        @Override
        public Optional<Match> findById(UUID id) {
            return store.stream().filter(m -> m.getId().equals(id)).findFirst();
        }

        @Override
        public List<Match> findByTournamentId(UUID tournamentId) {
            return store.stream().filter(m -> m.getTournamentId().equals(tournamentId)).toList();
        }

        @Override
        public List<Match> findByTournamentIdAndMatchStatus(UUID tournamentId, Match.MatchStatus status) {
            return store.stream()
                    .filter(m -> m.getTournamentId().equals(tournamentId) && m.getMatchStatus() == status)
                    .toList();
        }

        @Override
        public List<Match> findByTournamentIdAndKickoffTimeBetween(UUID tournamentId, Instant from, Instant to) {
            return store.stream()
                    .filter(m -> m.getTournamentId().equals(tournamentId)
                            && !m.getKickoffTime().isBefore(from)
                            && m.getKickoffTime().isBefore(to))
                    .toList();
        }

        @Override
        public List<Match> findByKickoffTimeBetween(Instant from, Instant to) {
            return store.stream()
                    .filter(m -> !m.getKickoffTime().isBefore(from) && m.getKickoffTime().isBefore(to))
                    .toList();
        }

        @Override
        public Optional<Match> findByExternalId(String externalId) { return Optional.empty(); }
    }

    static class InMemoryGroupRepository implements GroupRepositoryPort {
        private final Map<UUID, Group> store = new HashMap<>();

        @Override
        public Group save(Group group) { store.put(group.getId(), group); return group; }

        @Override
        public Optional<Group> findById(UUID id) { return Optional.ofNullable(store.get(id)); }

        @Override
        public Optional<Group> findByInviteCode(UUID inviteCode) { return Optional.empty(); }

        @Override
        public List<Group> findByOwnerId(UUID ownerId) { return List.of(); }
    }

    static class InMemoryGroupMemberRepository implements GroupMemberRepositoryPort {
        private final List<GroupMember> store = new ArrayList<>();

        void add(GroupMember member) { store.add(member); }

        @Override
        public GroupMember save(GroupMember member) { store.add(member); return member; }

        @Override
        public Optional<GroupMember> findById(UUID id) { return Optional.empty(); }

        @Override
        public List<GroupMember> findByGroupId(UUID groupId) {
            return store.stream().filter(m -> m.getGroupId().equals(groupId)).toList();
        }

        @Override
        public List<com.predictorama.backend.domain.entity.aggregate.GroupMemberView> findByGroupIdWithUsernames(UUID groupId) {
            return List.of();
        }

        @Override
        public List<GroupMember> findByUserId(UUID userId) {
            return store.stream().filter(m -> m.getUserId().equals(userId)).toList();
        }

        @Override
        public Optional<GroupMember> findByGroupIdAndUserId(UUID groupId, UUID userId) { return Optional.empty(); }

        @Override
        public void deleteByGroupIdAndUserId(UUID groupId, UUID userId) {}
    }

    static class InMemoryGroupTournamentRepository implements GroupTournamentRepositoryPort {
        private final Set<String> links = new HashSet<>();

        private String key(UUID groupId, UUID tournamentId) { return groupId + ":" + tournamentId; }

        @Override
        public List<UUID> findTournamentIdsByGroupId(UUID groupId) {
            return links.stream()
                    .filter(l -> l.startsWith(groupId + ":"))
                    .map(l -> UUID.fromString(l.substring(l.indexOf(':') + 1)))
                    .toList();
        }

        @Override
        public boolean existsByGroupIdAndTournamentId(UUID groupId, UUID tournamentId) {
            return links.contains(key(groupId, tournamentId));
        }

        @Override
        public void save(UUID groupId, UUID tournamentId) { links.add(key(groupId, tournamentId)); }

        @Override
        public void delete(UUID groupId, UUID tournamentId) { links.remove(key(groupId, tournamentId)); }
    }

    static class InMemoryTournamentRepository implements TournamentRepositoryPort {
        @Override
        public Tournament save(Tournament t) { return t; }

        @Override
        public Optional<Tournament> findById(UUID id) { return Optional.empty(); }

        @Override
        public Optional<Tournament> findByNameIgnoreCase(String name) { return Optional.empty(); }

        @Override
        public List<Tournament> findAll() { return List.of(); }
    }
}
