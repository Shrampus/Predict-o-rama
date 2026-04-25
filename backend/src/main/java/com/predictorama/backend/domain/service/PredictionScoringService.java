package com.predictorama.backend.domain.service;

import com.predictorama.backend.domain.entity.*;
import com.predictorama.backend.domain.port.persistence.MatchRepositoryPort;
import com.predictorama.backend.domain.port.persistence.PredictionRepositoryPort;
import com.predictorama.backend.domain.port.persistence.RulesetRepositoryPort;
import com.predictorama.backend.domain.service.scoring.ScoringRule;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PredictionScoringService {

    private static final Logger log = LoggerFactory.getLogger(PredictionScoringService.class);
    private final Map<String, ScoringRule> ruleByName;
    private final PredictionRepositoryPort predictionRepositoryPort;
    private final MatchRepositoryPort matchRepositoryPort;
    private final RulesetRepositoryPort rulesetRepositoryPort;

    public PredictionScoringService(List<ScoringRule> scoringRules,
                                    PredictionRepositoryPort predictionRepositoryPort,
                                    MatchRepositoryPort matchRepositoryPort,
                                    RulesetRepositoryPort rulesetRepositoryPort) {
        this.ruleByName = scoringRules.stream()
                .collect(Collectors.toMap(ScoringRule::name, r -> r));
        this.predictionRepositoryPort = predictionRepositoryPort;
        this.matchRepositoryPort = matchRepositoryPort;
        this.rulesetRepositoryPort = rulesetRepositoryPort;
    }

    @Transactional
    public void distributePredictionScores(UUID matchId) {
        var matchRepoPort = matchRepositoryPort.findById(matchId);
        if (matchRepoPort.isEmpty()) {
            throw new IllegalStateException("Match not found for matchId: " + matchId);
        }
        var match = matchRepoPort.get();

        var predictions = predictionRepositoryPort.findByMatchId(matchId);

        var actualScore = getActualScore(match);
        var actualWinner = match.getWinner();


        var grouped = predictions.stream()
                .collect(Collectors.groupingBy(Prediction::getGroupId));

        for (var entry : grouped.entrySet()) {
            Map<String, Integer> activeRules = rulesetRepositoryPort
                    .findByGroupIdAndTournamentId(entry.getKey(), match.getTournamentId())
                    .map(Ruleset::getRulePoints)
                    .orElseGet(() -> {
                        log.warn("No ruleset found, using default - groupId={}, tournamentId={}", entry.getKey(), match.getTournamentId());
                        return RulesetService.DEFAULT_RULE_POINTS;
                    });

            for (var prediction : entry.getValue()) {
                calculatePredictionResult(prediction, actualScore, actualWinner, activeRules);
            }
        }

    }


    @Async
    @Transactional
    public void recalculatePredictionScores(UUID groupId, UUID tournamentId, Ruleset ruleset) {
        List<Match> finishedMatches = matchRepositoryPort.findAllFinishedByTournamentId(tournamentId);

        List<Prediction> predictions = predictionRepositoryPort.findByGroupIdAndMatchIdIn(groupId, finishedMatches.stream().map(Match::getId).toList());

        var grouped = predictions.stream()
                .collect(Collectors.groupingBy(Prediction::getMatchId));

        Map<UUID, Match> matchById = finishedMatches.stream()
                .collect(Collectors.toMap(Match::getId, m -> m));

        for (var entry : grouped.entrySet()) {
            Map<String, Integer> activeRules = ruleset.getRulePoints();
            Match match = matchById.get(entry.getKey());

            Score actualScore = getActualScore(match);
            Winner actualWinner = match.getWinner();

            for (var prediction : entry.getValue()) {
                calculatePredictionResult(prediction, actualScore, actualWinner, activeRules);
            }
        }
    }

    private Score getActualScore(Match match){
        return match.getScores().stream()
                .filter(s -> s.getScoreType() == Score.ScoreType.FULL_TIME)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No full-time score found for matchId: " + match.getId()));
    }

    private void calculatePredictionResult(Prediction prediction, Score actualScore, Winner actualWinner, Map<String, Integer> activeRules){
        int totalScore = activeRules.entrySet().stream()
                .filter(entry -> ruleByName.containsKey(entry.getKey()))
                .filter(entry -> ruleByName.get(entry.getKey()).matches(prediction, actualScore, actualWinner))
                .mapToInt(Map.Entry::getValue)
                .sum();

        predictionRepositoryPort.updateResult(prediction.getId(), totalScore);
    }



}
