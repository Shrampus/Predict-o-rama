package com.predictorama.backend.domain.service;

import com.predictorama.backend.domain.entity.Group;
import com.predictorama.backend.domain.entity.GroupMember;
import com.predictorama.backend.domain.entity.Role;
import com.predictorama.backend.domain.entity.Ruleset;
import com.predictorama.backend.domain.entity.aggregate.GroupMemberView;
import com.predictorama.backend.domain.exception.GroupAccessDeniedException;
import com.predictorama.backend.domain.exception.TournamentNotLinkedException;
import com.predictorama.backend.domain.port.persistence.GroupMemberRepositoryPort;
import com.predictorama.backend.domain.port.persistence.GroupRepositoryPort;
import com.predictorama.backend.domain.port.persistence.GroupTournamentRepositoryPort;
import com.predictorama.backend.domain.port.persistence.RulesetRepositoryPort;
import com.predictorama.backend.domain.service.scoring.CorrectGoalDifferenceRule;
import com.predictorama.backend.domain.service.scoring.CorrectWinnerRule;
import com.predictorama.backend.domain.service.scoring.ExactScoreRule;
import com.predictorama.backend.domain.service.scoring.ScoringRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class RulesetServiceTest {

    private RulesetService rulesetService;
    private InMemoryRulesetRepository rulesetRepository;
    private InMemoryGroupTournamentRepository groupTournamentRepository;
    private InMemoryGroupRepository groupRepository;
    private InMemoryGroupMemberRepository groupMemberRepository;
    private PredictionScoringService predictionScoringService;

    private static final List<ScoringRule> ALL_SCORING_RULES = List.of(
            new CorrectWinnerRule(),
            new ExactScoreRule(),
            new CorrectGoalDifferenceRule()
    );

    @BeforeEach
    void setUp() {
        rulesetRepository = new InMemoryRulesetRepository();
        groupTournamentRepository = new InMemoryGroupTournamentRepository();
        groupRepository = new InMemoryGroupRepository();
        groupMemberRepository = new InMemoryGroupMemberRepository();
        predictionScoringService = mock(PredictionScoringService.class);
        AccessService accessService = new AccessService(groupRepository, groupMemberRepository);
        rulesetService = new RulesetService(
                rulesetRepository,
                groupTournamentRepository,
                accessService,
                predictionScoringService,
                ALL_SCORING_RULES
        );
    }

    @Test
    void getRuleset_returnsStoredRuleset_whenTournamentLinkedAndRulesetExists() {
        UUID userId = activeMember();
        UUID groupId = groupOf(userId);
        UUID tournamentId = UUID.randomUUID();
        groupTournamentRepository.save(groupId, tournamentId);
        Map<String, Integer> points = Map.of("CORRECT_WINNER", 2, "EXACT_SCORE", 5);
        rulesetRepository.upsertForGroupTournament(groupId, tournamentId, points);

        RulesetService.RulesetResult result = rulesetService.getRuleset(userId, groupId, tournamentId);

        assertThat(result.ruleset().getRulePoints()).containsAllEntriesOf(points);
    }

    @Test
    void getRuleset_returnsDefaultRulesetWithNullId_whenTournamentLinkedButNoRulesetStored() {
        UUID userId = activeMember();
        UUID groupId = groupOf(userId);
        UUID tournamentId = UUID.randomUUID();
        groupTournamentRepository.save(groupId, tournamentId);

        RulesetService.RulesetResult result = rulesetService.getRuleset(userId, groupId, tournamentId);

        assertThat(result.ruleset().getId()).isNull();
        assertThat(result.ruleset().getRulePoints()).isEqualTo(RulesetService.DEFAULT_RULE_POINTS);
    }

    @Test
    void getRuleset_throwsTournamentNotLinked_whenTournamentNotLinkedToGroup() {
        UUID userId = activeMember();
        UUID groupId = groupOf(userId);
        UUID unlinkedTournamentId = UUID.randomUUID();

        assertThatThrownBy(() -> rulesetService.getRuleset(userId, groupId, unlinkedTournamentId))
                .isInstanceOf(TournamentNotLinkedException.class);
    }

    @Test
    void getRuleset_throwsGroupAccessDenied_whenUserIsNotGroupMember() {
        UUID nonMemberId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        groupRepository.save(Group.builder().id(groupId).ownerId(UUID.randomUUID())
                .inviteCode(UUID.randomUUID()).name("G").build());
        UUID tournamentId = UUID.randomUUID();
        groupTournamentRepository.save(groupId, tournamentId);

        assertThatThrownBy(() -> rulesetService.getRuleset(nonMemberId, groupId, tournamentId))
                .isInstanceOf(GroupAccessDeniedException.class);
    }

    @Test
    void getRuleset_includesDisabledRules_forRulesNotInActiveRulePoints() {
        UUID userId = activeMember();
        UUID groupId = groupOf(userId);
        UUID tournamentId = UUID.randomUUID();
        groupTournamentRepository.save(groupId, tournamentId);
        rulesetRepository.upsertForGroupTournament(groupId, tournamentId,
                Map.of("CORRECT_WINNER", 1, "EXACT_SCORE", 3));

        RulesetService.RulesetResult result = rulesetService.getRuleset(userId, groupId, tournamentId);

        assertThat(result.disabledRules()).containsKey("CORRECT_GOAL_DIFFERENCE");
        assertThat(result.disabledRules()).doesNotContainKey("CORRECT_WINNER");
        assertThat(result.disabledRules()).doesNotContainKey("EXACT_SCORE");
    }

    @Test
    void updateRuleset_savesNewRulePointsAndTriggersScoreRecalculation() {
        UUID adminId = adminMember();
        UUID groupId = groupOf(adminId);
        UUID tournamentId = UUID.randomUUID();
        groupTournamentRepository.save(groupId, tournamentId);
        Map<String, Integer> newPoints = Map.of("EXACT_SCORE", 5);

        RulesetService.RulesetResult result = rulesetService.updateRuleset(adminId, groupId, tournamentId, newPoints);

        assertThat(result.ruleset().getRulePoints()).isEqualTo(newPoints);
        verify(predictionScoringService).recalculatePredictionScores(eq(groupId), eq(tournamentId), any());
    }

    @Test
    void updateRuleset_throwsGroupAccessDenied_whenCallerIsNotAdmin() {
        UUID userId = activeMember();
        UUID groupId = groupOf(userId);
        UUID tournamentId = UUID.randomUUID();

        assertThatThrownBy(() -> rulesetService.updateRuleset(userId, groupId, tournamentId, Map.of("EXACT_SCORE", 3)))
                .isInstanceOf(GroupAccessDeniedException.class);
    }

    @Test
    void setDefaultRulesetForGroupTournament_persistsDefaultRulePoints() {
        UUID groupId = UUID.randomUUID();
        UUID tournamentId = UUID.randomUUID();

        rulesetService.setDefaultRulesetForGroupTournament(groupId, tournamentId);

        Optional<Ruleset> saved = rulesetRepository.findByGroupIdAndTournamentId(groupId, tournamentId);
        assertThat(saved).isPresent();
        assertThat(saved.get().getRulePoints()).isEqualTo(RulesetService.DEFAULT_RULE_POINTS);
    }

    // --- helpers ---

    private UUID activeMember() {
        UUID userId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        groupRepository.save(Group.builder().id(groupId).ownerId(userId)
                .inviteCode(UUID.randomUUID()).name("G").build());
        groupMemberRepository.save(GroupMember.builder()
                .id(UUID.randomUUID()).userId(userId).groupId(groupId)
                .memberRole(Role.USER).status(GroupMember.MemberStatus.ACTIVE).build());
        return userId;
    }

    private UUID adminMember() {
        UUID userId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        groupRepository.save(Group.builder().id(groupId).ownerId(userId)
                .inviteCode(UUID.randomUUID()).name("G").build());
        groupMemberRepository.save(GroupMember.builder()
                .id(UUID.randomUUID()).userId(userId).groupId(groupId)
                .memberRole(Role.ADMIN).status(GroupMember.MemberStatus.ACTIVE).build());
        return userId;
    }

    private UUID groupOf(UUID userId) {
        return groupMemberRepository.findByUserId(userId).stream()
                .findFirst()
                .map(GroupMember::getGroupId)
                .orElseThrow();
    }

    // --- In-memory stubs ---

    static class InMemoryRulesetRepository implements RulesetRepositoryPort {
        private final Map<String, Ruleset> store = new HashMap<>();

        private String key(UUID groupId, UUID tournamentId) {
            return groupId + ":" + tournamentId;
        }

        @Override
        public Optional<Ruleset> findByGroupIdAndTournamentId(UUID groupId, UUID tournamentId) {
            return Optional.ofNullable(store.get(key(groupId, tournamentId)));
        }

        @Override
        public Ruleset upsertForGroupTournament(UUID groupId, UUID tournamentId, Map<String, Integer> rulePoints) {
            Ruleset ruleset = Ruleset.builder().id(UUID.randomUUID()).rulePoints(rulePoints).build();
            store.put(key(groupId, tournamentId), ruleset);
            return ruleset;
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
                    .filter(l -> l.startsWith(groupId + ":"))
                    .map(l -> UUID.fromString(l.substring(l.indexOf(':') + 1)))
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
                    .map(m -> new GroupMemberView(m, m.getUserId().toString()))
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
}
