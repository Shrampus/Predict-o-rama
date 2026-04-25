package com.predictorama.backend.domain.service;

import com.predictorama.backend.domain.entity.Match;
import com.predictorama.backend.domain.entity.Prediction;
import com.predictorama.backend.domain.entity.Ruleset;
import com.predictorama.backend.domain.entity.Score;
import com.predictorama.backend.domain.entity.Team;
import com.predictorama.backend.domain.entity.Winner;
import com.predictorama.backend.domain.port.persistence.MatchRepositoryPort;
import com.predictorama.backend.domain.port.persistence.PredictionRepositoryPort;
import com.predictorama.backend.domain.port.persistence.RulesetRepositoryPort;
import com.predictorama.backend.domain.service.scoring.CorrectWinnerRule;
import com.predictorama.backend.domain.service.scoring.ExactScoreRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PredictionScoringServiceTest {

    private PredictionScoringService scoringService;
    private InMemoryPredictionRepository predictionRepository;
    private InMemoryMatchRepository matchRepository;
    private InMemoryRulesetRepository rulesetRepository;

    @BeforeEach
    void setUp() {
        predictionRepository = new InMemoryPredictionRepository();
        matchRepository = new InMemoryMatchRepository();
        rulesetRepository = new InMemoryRulesetRepository();
        scoringService = new PredictionScoringService(
                List.of(new ExactScoreRule(), new CorrectWinnerRule()),
                predictionRepository,
                matchRepository,
                rulesetRepository
        );
    }

    @Test
    void distributePredictionScores_calculatesPointsForEachPrediction() {
        UUID groupId = UUID.randomUUID();
        UUID tournamentId = UUID.randomUUID();
        UUID matchId = UUID.randomUUID();
        UUID exactPredictionId = UUID.randomUUID();
        UUID winnerOnlyPredictionId = UUID.randomUUID();
        UUID missedPredictionId = UUID.randomUUID();

        rulesetRepository.seed(groupId, tournamentId,
                Ruleset.builder().id(UUID.randomUUID()).rulePoints(Map.of("EXACT_SCORE", 3, "CORRECT_WINNER", 1)).build());
        matchRepository.save(match(matchId, tournamentId, score(2, 1), Winner.HOME));
        predictionRepository.save(prediction(exactPredictionId, UUID.randomUUID(), groupId, matchId, score(2, 1), Winner.HOME));
        predictionRepository.save(prediction(winnerOnlyPredictionId, UUID.randomUUID(), groupId, matchId, score(1, 0), Winner.HOME));
        predictionRepository.save(prediction(missedPredictionId, UUID.randomUUID(), groupId, matchId, score(1, 2), Winner.AWAY));

        scoringService.distributePredictionScores(matchId);

        assertThat(predictionRepository.findById(exactPredictionId)).get()
                .extracting(Prediction::getResult).isEqualTo(4);
        assertThat(predictionRepository.findById(winnerOnlyPredictionId)).get()
                .extracting(Prediction::getResult).isEqualTo(1);
        assertThat(predictionRepository.findById(missedPredictionId)).get()
                .extracting(Prediction::getResult).isEqualTo(0);
    }

    @Test
    void distributePredictionScores_respectsGroupRulesetWhenScoringPredictions() {
        UUID groupId = UUID.randomUUID();
        UUID tournamentId = UUID.randomUUID();
        UUID matchId = UUID.randomUUID();
        UUID exactPredictionId = UUID.randomUUID();

        rulesetRepository.seed(groupId, tournamentId,
                Ruleset.builder().id(UUID.randomUUID()).rulePoints(Map.of("EXACT_SCORE", 3)).build());
        matchRepository.save(match(matchId, tournamentId, score(2, 1), Winner.HOME));
        predictionRepository.save(prediction(exactPredictionId, UUID.randomUUID(), groupId, matchId, score(2, 1), Winner.HOME));

        scoringService.distributePredictionScores(matchId);

        assertThat(predictionRepository.findById(exactPredictionId)).get()
                .extracting(Prediction::getResult).isEqualTo(3);
    }

    private Match match(UUID id, UUID tournamentId, Score fullTimeScore, Winner winner) {
        return Match.builder()
                .id(id)
                .tournamentId(tournamentId)
                .name("Match")
                .homeTeam(Team.builder().id(UUID.randomUUID()).name("Home").build())
                .awayTeam(Team.builder().id(UUID.randomUUID()).name("Away").build())
                .matchStatus(Match.MatchStatus.COMPLETED)
                .kickoffTime(Instant.now())
                .scores(List.of(fullTimeScore))
                .winner(winner)
                .build();
    }

    private Prediction prediction(UUID id, UUID userId, UUID groupId, UUID matchId, Score predictedScore, Winner predictedWinner) {
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
        public void updateResult(UUID predictionId, int result) {
            Prediction existing = store.get(predictionId);
            if (existing != null) {
                store.put(predictionId, Prediction.builder()
                        .id(existing.getId())
                        .userId(existing.getUserId())
                        .matchId(existing.getMatchId())
                        .groupId(existing.getGroupId())
                        .predictedScores(existing.getPredictedScores())
                        .predictedWinner(existing.getPredictedWinner())
                        .submittedAt(existing.getSubmittedAt())
                        .result(result)
                        .build());
            }
        }

        @Override
        public Optional<Prediction> findById(UUID id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public Optional<Prediction> findByUserIdAndMatchIdAndGroupId(UUID userId, UUID matchId, UUID groupId) {
            return store.values().stream()
                    .filter(p -> p.getUserId().equals(userId) && p.getMatchId().equals(matchId) && p.getGroupId().equals(groupId))
                    .findFirst();
        }

        @Override
        public List<Prediction> findByMatchIdAndGroupId(UUID matchId, UUID groupId) {
            return store.values().stream()
                    .filter(p -> p.getMatchId().equals(matchId) && p.getGroupId().equals(groupId))
                    .toList();
        }

        @Override
        public List<Prediction> findByUserIdAndGroupId(UUID userId, UUID groupId) {
            return store.values().stream()
                    .filter(p -> p.getUserId().equals(userId) && p.getGroupId().equals(groupId))
                    .toList();
        }

        @Override
        public List<Prediction> findByUserId(UUID userId) {
            return store.values().stream().filter(p -> p.getUserId().equals(userId)).toList();
        }

        @Override
        public List<Prediction> findByGroupId(UUID groupId) {
            return store.values().stream().filter(p -> p.getGroupId().equals(groupId)).toList();
        }

        @Override
        public List<Prediction> findByMatchId(UUID matchId) {
            return store.values().stream().filter(p -> p.getMatchId().equals(matchId)).toList();
        }

        @Override
        public List<Prediction> findByGroupIdAndMatchIdIn(UUID groupId, Collection<UUID> matchIds) {
            return store.values().stream()
                    .filter(p -> p.getGroupId().equals(groupId) && matchIds.contains(p.getMatchId()))
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
            return store.values().stream().filter(m -> m.getTournamentId().equals(tournamentId)).toList();
        }

        @Override
        public List<Match> findByTournamentIdAndMatchStatus(UUID tournamentId, Match.MatchStatus matchStatus) {
            return findByTournamentId(tournamentId).stream().filter(m -> m.getMatchStatus() == matchStatus).toList();
        }

        @Override
        public List<Match> findByTournamentIdAndKickoffTimeBetween(UUID tournamentId, Instant from, Instant to) {
            return findByTournamentId(tournamentId).stream()
                    .filter(m -> !m.getKickoffTime().isBefore(from) && !m.getKickoffTime().isAfter(to))
                    .toList();
        }

        @Override
        public List<Match> findByKickoffTimeBetween(Instant from, Instant to) {
            return store.values().stream()
                    .filter(m -> !m.getKickoffTime().isBefore(from) && !m.getKickoffTime().isAfter(to))
                    .toList();
        }

        @Override
        public Optional<Match> findByExternalId(String externalId) {
            return Optional.empty();
        }

        @Override
        public List<Match> findAllFinishedByTournamentId(UUID tournamentId) {
            return findByTournamentIdAndMatchStatus(tournamentId, Match.MatchStatus.COMPLETED);
        }
    }

    static class InMemoryRulesetRepository implements RulesetRepositoryPort {
        private final Map<String, Ruleset> store = new HashMap<>();

        void seed(UUID groupId, UUID tournamentId, Ruleset ruleset) {
            store.put(key(groupId, tournamentId), ruleset);
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

        private String key(UUID groupId, UUID tournamentId) {
            return groupId + "|" + tournamentId;
        }
    }
}
