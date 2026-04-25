package com.predictorama.backend.domain.service;

import com.predictorama.backend.domain.entity.Group;
import com.predictorama.backend.domain.entity.Match;
import com.predictorama.backend.domain.entity.Prediction;
import com.predictorama.backend.domain.entity.Ruleset;
import com.predictorama.backend.domain.entity.Score;
import com.predictorama.backend.domain.entity.Team;
import com.predictorama.backend.domain.entity.Winner;
import com.predictorama.backend.domain.port.persistence.GroupRepositoryPort;
import com.predictorama.backend.domain.port.persistence.MatchRepositoryPort;
import com.predictorama.backend.domain.port.persistence.PredictionRepositoryPort;
import com.predictorama.backend.domain.port.persistence.RulesetRepositoryPort;
import com.predictorama.backend.domain.service.scoring.CorrectWinnerRule;
import com.predictorama.backend.domain.service.scoring.ExactScoreRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PredictionScoringServiceTest {

    private PredictionScoringService scoringService;
    private InMemoryPredictionRepository predictionRepository;
    private InMemoryMatchRepository matchRepository;
    private InMemoryGroupRepository groupRepository;
    private InMemoryRulesetRepository rulesetRepository;

    @BeforeEach
    void setUp() {
        predictionRepository = new InMemoryPredictionRepository();
        matchRepository = new InMemoryMatchRepository();
        groupRepository = new InMemoryGroupRepository();
        rulesetRepository = new InMemoryRulesetRepository();
        scoringService = new PredictionScoringService(
                List.of(new ExactScoreRule(), new CorrectWinnerRule()),
                predictionRepository,
                matchRepository,
                groupRepository,
                rulesetRepository
        );
    }

    @Test
    void distributePredictionScores_calculatesPointsForEachPrediction() {
        UUID defaultRulesetId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        UUID matchId = UUID.randomUUID();
        UUID exactPredictionId = UUID.randomUUID();
        UUID winnerOnlyPredictionId = UUID.randomUUID();
        UUID missedPredictionId = UUID.randomUUID();

        rulesetRepository.save(Ruleset.builder()
                .id(defaultRulesetId)
                .name("Default")
                .ruleNames(Set.of("EXACT_SCORE", "CORRECT_WINNER"))
                .build());
        groupRepository.save(Group.builder()
                .id(groupId)
                .ownerId(UUID.randomUUID())
                .inviteCode(UUID.randomUUID())
                .rulesetId(defaultRulesetId)
                .name("Legends")
                .build());
        matchRepository.save(match(matchId, score(2, 1), Winner.HOME));
        predictionRepository.save(prediction(
                exactPredictionId,
                UUID.randomUUID(),
                groupId,
                matchId,
                score(2, 1),
                Winner.HOME
        ));
        predictionRepository.save(prediction(
                winnerOnlyPredictionId,
                UUID.randomUUID(),
                groupId,
                matchId,
                score(1, 0),
                Winner.HOME
        ));
        predictionRepository.save(prediction(
                missedPredictionId,
                UUID.randomUUID(),
                groupId,
                matchId,
                score(1, 2),
                Winner.AWAY
        ));

        scoringService.distributePredictionScores(matchId);

        assertThat(predictionRepository.findById(exactPredictionId)).get()
                .extracting(Prediction::getResult)
                .isEqualTo(4);
        assertThat(predictionRepository.findById(winnerOnlyPredictionId)).get()
                .extracting(Prediction::getResult)
                .isEqualTo(1);
        assertThat(predictionRepository.findById(missedPredictionId)).get()
                .extracting(Prediction::getResult)
                .isEqualTo(0);
    }

    @Test
    void distributePredictionScores_respectsGroupRulesetWhenScoringPredictions() {
        UUID exactScoreOnlyRulesetId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        UUID matchId = UUID.randomUUID();
        UUID exactPredictionId = UUID.randomUUID();

        rulesetRepository.save(Ruleset.builder()
                .id(exactScoreOnlyRulesetId)
                .name("Exact only")
                .ruleNames(Set.of("EXACT_SCORE"))
                .build());
        groupRepository.save(Group.builder()
                .id(groupId)
                .ownerId(UUID.randomUUID())
                .inviteCode(UUID.randomUUID())
                .rulesetId(exactScoreOnlyRulesetId)
                .name("Exact gang")
                .build());
        matchRepository.save(match(matchId, score(2, 1), Winner.HOME));
        predictionRepository.save(prediction(
                exactPredictionId,
                UUID.randomUUID(),
                groupId,
                matchId,
                score(2, 1),
                Winner.HOME
        ));

        scoringService.distributePredictionScores(matchId);

        assertThat(predictionRepository.findById(exactPredictionId)).get()
                .extracting(Prediction::getResult)
                .isEqualTo(3);
    }

    private Match match(UUID id, Score fullTimeScore, Winner winner) {
        return Match.builder()
                .id(id)
                .tournamentId(UUID.randomUUID())
                .name("Match")
                .homeTeam(Team.builder().id(UUID.randomUUID()).name("Home").build())
                .awayTeam(Team.builder().id(UUID.randomUUID()).name("Away").build())
                .matchStatus(Match.MatchStatus.COMPLETED)
                .kickoffTime(Instant.now())
                .scores(List.of(fullTimeScore))
                .winner(winner)
                .build();
    }

    private Prediction prediction(
            UUID id,
            UUID userId,
            UUID groupId,
            UUID matchId,
            Score predictedScore,
            Winner predictedWinner
    ) {
        return Prediction.builder()
                .id(id)
                .userId(userId)
                .groupId(groupId)
                .matchId(matchId)
                .predictedScores(List.of(predictedScore))
                .predictedWinner(predictedWinner)
                .submittedAt(Instant.now())
                .build();
    }

    private Score score(int homeScore, int awayScore) {
        return Score.builder()
                .homeScore(homeScore)
                .awayScore(awayScore)
                .scoreType(Score.ScoreType.FULL_TIME)
                .build();
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
            return store.values().stream()
                    .filter(group -> group.getInviteCode().equals(inviteCode))
                    .findFirst();
        }

        @Override
        public List<Group> findByOwnerId(UUID ownerId) {
            return store.values().stream()
                    .filter(group -> group.getOwnerId().equals(ownerId))
                    .toList();
        }
    }

    static class InMemoryRulesetRepository implements RulesetRepositoryPort {
        private final Map<UUID, Ruleset> store = new HashMap<>();

        @Override
        public Ruleset save(Ruleset ruleset) {
            store.put(ruleset.getId(), ruleset);
            return ruleset;
        }

        @Override
        public Optional<Ruleset> findById(UUID id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public Optional<Ruleset> findByName(String name) {
            return store.values().stream()
                    .filter(ruleset -> ruleset.getName().equals(name))
                    .findFirst();
        }

        @Override
        public List<Ruleset> findAll() {
            return new ArrayList<>(store.values());
        }
    }
}
