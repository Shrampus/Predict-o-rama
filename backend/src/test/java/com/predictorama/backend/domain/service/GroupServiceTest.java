package com.predictorama.backend.domain.service;

import com.predictorama.backend.domain.entity.Group;
import com.predictorama.backend.domain.entity.GroupMember;
import com.predictorama.backend.domain.entity.Match;
import com.predictorama.backend.domain.entity.Prediction;
import com.predictorama.backend.domain.entity.Role;
import com.predictorama.backend.domain.entity.Score;
import com.predictorama.backend.domain.entity.Team;
import com.predictorama.backend.domain.entity.Tournament;
import com.predictorama.backend.domain.entity.User;
import com.predictorama.backend.domain.entity.Winner;
import com.predictorama.backend.domain.entity.aggregate.GroupLeaderboardView;
import com.predictorama.backend.domain.entity.aggregate.GroupMemberView;
import com.predictorama.backend.domain.exception.GroupNotFoundException;
import com.predictorama.backend.domain.port.persistence.GroupTournamentRepositoryPort;
import com.predictorama.backend.domain.port.persistence.GroupMemberRepositoryPort;
import com.predictorama.backend.domain.port.persistence.GroupRepositoryPort;
import com.predictorama.backend.domain.port.persistence.MatchRepositoryPort;
import com.predictorama.backend.domain.port.persistence.PredictionRepositoryPort;
import com.predictorama.backend.domain.port.persistence.TournamentRepositoryPort;
import com.predictorama.backend.domain.port.persistence.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

class GroupServiceTest {

    private GroupService groupService;
    private InMemoryGroupRepository groupRepository;
    private InMemoryGroupMemberRepository groupMemberRepository;
    private InMemoryUserRepository userRepository;
    private InMemoryTournamentRepository tournamentRepository;
    private InMemoryGroupTournamentRepository groupTournamentRepository;
    private InMemoryMatchRepository matchRepository;
    private InMemoryPredictionRepository predictionRepository;

    @BeforeEach
    void setUp() {
        groupRepository = new InMemoryGroupRepository();
        groupMemberRepository = new InMemoryGroupMemberRepository();
        userRepository = new InMemoryUserRepository();
        tournamentRepository = new InMemoryTournamentRepository();
        groupTournamentRepository = new InMemoryGroupTournamentRepository();
        matchRepository = new InMemoryMatchRepository();
        predictionRepository = new InMemoryPredictionRepository();
        groupService = new GroupService(
                groupRepository,
                groupMemberRepository,
                userRepository,
                tournamentRepository,
                groupTournamentRepository,
                matchRepository,
                predictionRepository
        );
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

        List<GroupMemberView> members = groupService.getGroupMembers(ownerId, group.getId());

        assertThat(members).hasSize(2);
    }

    @Test
    void getGroupMembers_unknownGroup_throwsNotFound() {
        assertThatThrownBy(() -> groupService.getGroupMembers(UUID.randomUUID(), UUID.randomUUID()))
                .isInstanceOf(GroupNotFoundException.class);
    }

    @Test
    void removeTournamentFromGroup_existingLink_removesLink() {
        UUID adminId = UUID.randomUUID();
        Group group = groupService.createGroup(adminId, "Legends", "Best group");
        UUID tournamentId = UUID.randomUUID();
        groupTournamentRepository.save(group.getId(), tournamentId);

        groupService.removeTournamentFromGroup(adminId, group.getId(), tournamentId);

        assertThat(groupTournamentRepository.existsByGroupIdAndTournamentId(group.getId(), tournamentId)).isFalse();
    }

    @Test
    void getGroupLeaderboards_sumsScoredPredictionResultsSeparatelyForEachTournament() {
        UUID adminId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        Group group = groupService.createGroup(adminId, "Legends", "Best group");
        groupService.joinGroup(memberId, group.getInviteCode());

        Tournament championsLeague = tournament(UUID.randomUUID(), "Champions League");
        Tournament premierLeague = tournament(UUID.randomUUID(), "Premier League");
        tournamentRepository.save(championsLeague);
        tournamentRepository.save(premierLeague);
        groupTournamentRepository.save(group.getId(), championsLeague.getId());
        groupTournamentRepository.save(group.getId(), premierLeague.getId());

        Match championsLeagueMatch = match(UUID.randomUUID(), championsLeague.getId());
        Match secondChampionsLeagueMatch = match(UUID.randomUUID(), championsLeague.getId());
        Match premierLeagueMatch = match(UUID.randomUUID(), premierLeague.getId());
        matchRepository.save(championsLeagueMatch);
        matchRepository.save(secondChampionsLeagueMatch);
        matchRepository.save(premierLeagueMatch);

        predictionRepository.save(prediction(adminId, group.getId(), championsLeagueMatch.getId(), 3));
        predictionRepository.save(prediction(adminId, group.getId(), secondChampionsLeagueMatch.getId(), 1));
        predictionRepository.save(prediction(memberId, group.getId(), championsLeagueMatch.getId(), 5));
        predictionRepository.save(prediction(memberId, group.getId(), secondChampionsLeagueMatch.getId(), null));
        predictionRepository.save(prediction(adminId, group.getId(), premierLeagueMatch.getId(), 2));

        List<GroupLeaderboardView> leaderboards = groupService.getGroupLeaderboards(adminId, group.getId());

        assertThat(leaderboards).hasSize(2);
        GroupLeaderboardView championsLeagueLeaderboard = leaderboards.stream()
                .filter(leaderboard -> leaderboard.tournament().getId().equals(championsLeague.getId()))
                .findFirst()
                .orElseThrow();
        GroupLeaderboardView premierLeagueLeaderboard = leaderboards.stream()
                .filter(leaderboard -> leaderboard.tournament().getId().equals(premierLeague.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(championsLeagueLeaderboard.entries())
                .extracting(
                        GroupLeaderboardView.Entry::userId,
                        GroupLeaderboardView.Entry::totalScore,
                        GroupLeaderboardView.Entry::scoredPredictions,
                        GroupLeaderboardView.Entry::totalPredictions
                )
                .containsExactly(
                        tuple(memberId, 5, 1, 2),
                        tuple(adminId, 4, 2, 2)
                );
        assertThat(premierLeagueLeaderboard.entries())
                .extracting(
                        GroupLeaderboardView.Entry::userId,
                        GroupLeaderboardView.Entry::totalScore,
                        GroupLeaderboardView.Entry::scoredPredictions,
                        GroupLeaderboardView.Entry::totalPredictions
                )
                .containsExactly(
                        tuple(adminId, 2, 1, 1),
                        tuple(memberId, 0, 0, 0)
                );
    }

    private Tournament tournament(UUID id, String name) {
        return Tournament.builder()
                .id(id)
                .name(name)
                .seasonLabel(name)
                .sport(Tournament.Sport.FOOTBALL)
                .build();
    }

    private Match match(UUID id, UUID tournamentId) {
        Team homeTeam = Team.builder().id(UUID.randomUUID()).name("Home").build();
        Team awayTeam = Team.builder().id(UUID.randomUUID()).name("Away").build();
        return Match.builder()
                .id(id)
                .tournamentId(tournamentId)
                .name("Match")
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .matchStatus(Match.MatchStatus.COMPLETED)
                .kickoffTime(Instant.now())
                .scores(List.of(Score.builder()
                        .homeScore(2)
                        .awayScore(1)
                        .scoreType(Score.ScoreType.FULL_TIME)
                        .build()))
                .winner(Winner.HOME)
                .build();
    }

    private Prediction prediction(UUID userId, UUID groupId, UUID matchId, Integer result) {
        return Prediction.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .groupId(groupId)
                .matchId(matchId)
                .predictedScores(List.of())
                .submittedAt(Instant.now())
                .result(result)
                .build();
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
        public List<GroupMemberView> findByGroupIdWithUsernames(UUID groupId) {
            return findByGroupId(groupId).stream()
                    .map(member -> new GroupMemberView(member, member.getUserId().toString()))
                    .toList();
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

    static class InMemoryUserRepository implements UserRepositoryPort {
        @Override
        public User save(User user) {
            return user;
        }

        @Override
        public Optional<User> findById(UUID id) {
            return Optional.empty();
        }

        @Override
        public Optional<User> findByUsername(String username) {
            return Optional.empty();
        }

        @Override
        public Optional<User> findByEmail(String email) {
            return Optional.empty();
        }

        @Override
        public Optional<User> findByGoogleId(String googleId) {
            return Optional.empty();
        }

        @Override
        public boolean existsByUsername(String username) {
            return false;
        }

        @Override
        public boolean existsByEmail(String email) {
            return false;
        }
    }

    static class InMemoryTournamentRepository implements TournamentRepositoryPort {
        private final Map<UUID, Tournament> store = new HashMap<>();

        @Override
        public Tournament save(Tournament tournament) {
            store.put(tournament.getId(), tournament);
            return tournament;
        }

        @Override
        public Optional<Tournament> findById(UUID id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public Optional<Tournament> findByNameIgnoreCase(String name) {
            return store.values().stream()
                    .filter(tournament -> tournament.getName().equalsIgnoreCase(name))
                    .findFirst();
        }

        @Override
        public List<Tournament> findAll() {
            return new ArrayList<>(store.values());
        }

        @Override
        public List<Tournament> findAllById(Set<UUID> ids) {
            return List.of();
        }
    }

    static class InMemoryGroupTournamentRepository implements GroupTournamentRepositoryPort {
        private final Set<String> links = new HashSet<>();

        private String key(UUID groupId, UUID tournamentId) {
            return groupId + ":" + tournamentId;
        }

        @Override
        public List<UUID> findTournamentIdsByGroupId(UUID groupId) {
            return links.stream()
                    .filter(link -> link.startsWith(groupId + ":"))
                    .map(link -> UUID.fromString(link.substring(link.indexOf(':') + 1)))
                    .toList();
        }

        @Override
        public boolean existsByGroupIdAndTournamentId(UUID groupId, UUID tournamentId) {
            return links.contains(key(groupId, tournamentId));
        }

        @Override
        public void save(UUID groupId, UUID tournamentId) {
            links.add(key(groupId, tournamentId));
        }

        @Override
        public void delete(UUID groupId, UUID tournamentId) {
            links.remove(key(groupId, tournamentId));
        }
    }

    static class InMemoryMatchRepository implements MatchRepositoryPort {
        private final Map<UUID, Match> store = new HashMap<>();

        @Override
        public Match save(Match match) {
            store.put(match.getId(), match);
            return match;
        }

        @Override
        public Optional<Match> findById(UUID id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public List<Match> findByTournamentId(UUID tournamentId) {
            return store.values().stream()
                    .filter(match -> match.getTournamentId().equals(tournamentId))
                    .toList();
        }

        @Override
        public List<Match> findByTournamentIdAndMatchStatus(UUID tournamentId, Match.MatchStatus matchStatus) {
            return findByTournamentId(tournamentId).stream()
                    .filter(match -> match.getMatchStatus() == matchStatus)
                    .toList();
        }

        @Override
        public List<Match> findByTournamentIdAndKickoffTimeBetween(UUID tournamentId, Instant from, Instant to) {
            return findByTournamentId(tournamentId).stream()
                    .filter(match -> !match.getKickoffTime().isBefore(from) && !match.getKickoffTime().isAfter(to))
                    .toList();
        }

        @Override
        public List<Match> findByKickoffTimeBetween(Instant from, Instant to) {
            return store.values().stream()
                    .filter(match -> !match.getKickoffTime().isBefore(from) && !match.getKickoffTime().isAfter(to))
                    .toList();
        }

        @Override
        public Optional<Match> findByExternalId(String externalId) {
            return Optional.empty();
        }
    }

    static class InMemoryPredictionRepository implements PredictionRepositoryPort {
        private final Map<UUID, Prediction> store = new HashMap<>();

        @Override
        public Prediction save(Prediction prediction) {
            store.put(prediction.getId(), prediction);
            return prediction;
        }

        @Override
        public Optional<Prediction> findById(UUID id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public Optional<Prediction> findByUserIdAndMatchIdAndGroupId(UUID userId, UUID matchId, UUID groupId) {
            return store.values().stream()
                    .filter(prediction -> prediction.getUserId().equals(userId))
                    .filter(prediction -> prediction.getMatchId().equals(matchId))
                    .filter(prediction -> prediction.getGroupId().equals(groupId))
                    .findFirst();
        }

        @Override
        public List<Prediction> findByMatchIdAndGroupId(UUID matchId, UUID groupId) {
            return store.values().stream()
                    .filter(prediction -> prediction.getMatchId().equals(matchId))
                    .filter(prediction -> prediction.getGroupId().equals(groupId))
                    .toList();
        }

        @Override
        public List<Prediction> findByUserIdAndGroupId(UUID userId, UUID groupId) {
            return store.values().stream()
                    .filter(prediction -> prediction.getUserId().equals(userId))
                    .filter(prediction -> prediction.getGroupId().equals(groupId))
                    .toList();
        }

        @Override
        public List<Prediction> findByUserId(UUID userId) {
            return store.values().stream()
                    .filter(prediction -> prediction.getUserId().equals(userId))
                    .toList();
        }

        @Override
        public List<Prediction> findByGroupId(UUID groupId) {
            return store.values().stream()
                    .filter(prediction -> prediction.getGroupId().equals(groupId))
                    .toList();
        }

        @Override
        public List<Prediction> findByMatchId(UUID matchId) {
            return store.values().stream()
                    .filter(prediction -> prediction.getMatchId().equals(matchId))
                    .toList();
        }
    }
}
