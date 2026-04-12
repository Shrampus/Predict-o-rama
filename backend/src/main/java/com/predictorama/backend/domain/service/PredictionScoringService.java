package com.predictorama.backend.domain.service;

import com.predictorama.backend.domain.entity.Prediction;
import com.predictorama.backend.domain.entity.Score;
import com.predictorama.backend.domain.entity.Winner;
import com.predictorama.backend.domain.port.persistence.GroupRepositoryPort;
import com.predictorama.backend.domain.port.persistence.MatchRepositoryPort;
import com.predictorama.backend.domain.port.persistence.PredictionRepositoryPort;
import com.predictorama.backend.domain.port.persistence.RulesetRepositoryPort;
import com.predictorama.backend.domain.service.scoring.ScoringRule;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PredictionScoringService {

    private static final Logger log = LoggerFactory.getLogger(PredictionScoringService.class);

    private final List<ScoringRule> scoringRules;

    private final PredictionRepositoryPort predictionRepositoryPort;

    private final MatchRepositoryPort matchRepositoryPort;

    private final GroupRepositoryPort groupRepositoryPort;

    private final RulesetRepositoryPort rulesetRepositoryPort;

    public void distributePredictionScores(UUID matchId) {
        var matchRepoPort = matchRepositoryPort.findById(matchId);
        if (matchRepoPort.isEmpty()) {
            throw new IllegalStateException("Match not found for matchId: " + matchId);
        }
        var match = matchRepoPort.get();

        var predictions = predictionRepositoryPort.findByMatchId(matchId);

        for (var prediction : predictions) {
            var groupRepoPort = groupRepositoryPort.findById(prediction.getGroupId())
                    .orElse(null);

            List<ScoringRule> activeRules;

            if (groupRepoPort == null || groupRepoPort.getRulesetId() == null) {
                log.warn("No group or ruleset found for groupId={}, using default ruleset", prediction.getGroupId());
                activeRules = scoringRules;
            } else {
                var rulesetRepoPort = rulesetRepositoryPort.findById(groupRepoPort.getRulesetId())
                        .orElseThrow(() -> new IllegalStateException(
                                "Ruleset not found for rulesetId:" + groupRepoPort.getRulesetId()));

                activeRules = scoringRules.stream()
                        .filter(r -> rulesetRepoPort.getRuleNames().contains(r.name()))
                        .toList();
            }

            var actualScore = match.getScores().stream()
            .filter( s -> s.getScoreType() == Score.ScoreType.FULL_TIME)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No full-time score found for matchId: " + match.getId()));
            var actualWinner = match.getWinner();
            int totalScore = 0;

            for (var rule : activeRules) {
                log.debug("Evaluating rule {} for predictionId={}", rule.name(), prediction.getId());
                totalScore += rule.evaluate(prediction, actualScore, actualWinner);
            }
            Prediction scored = Prediction.builder()
                    .id(prediction.getId())
                    .userId(prediction.getUserId())
                    .matchId(prediction.getMatchId())
                    .groupId(prediction.getGroupId())
                    .predictedScores(prediction.getPredictedScores())
                    .predictedWinner(prediction.getPredictedWinner())
                    .submittedAt(prediction.getSubmittedAt())
                    .result(totalScore)
                    .build();
            predictionRepositoryPort.save(scored);
        }
    }

}
