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

    public void distributePredictionScores(UUID matchId, UUID groupId) {
        var matchOpt = matchRepositoryPort.findById(matchId);
        if (matchOpt.isEmpty()) {
            throw new IllegalStateException("Match not found for matchId: " + matchId);
        }
        var match = matchOpt.get();

        var predictions = predictionRepositoryPort.findByMatchIdAndGroupId(matchId, groupId);

        var groupOpt = groupRepositoryPort.findById(groupId);
        if (groupOpt.isEmpty()) {
            throw new IllegalStateException("Group not found for groupId: " + groupId);
        }
        var group = groupOpt.get();

        List<ScoringRule> activeRules;
        if (group.getRulesetId() == null) {
            log.warn("No ruleset for groupId={}, applying all rules", groupId);
            activeRules = scoringRules;
        } else {
            var ruleset = rulesetRepositoryPort.findById(group.getRulesetId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Ruleset not found for rulesetId: " + group.getRulesetId()));
            activeRules = scoringRules.stream()
                    .filter(r -> ruleset.getRuleNames().contains(r.name()))
                    .toList();
        }

        Score actualScore = match.getScores().stream()
                .filter(s -> s.getScoreType() == Score.ScoreType.FULL_TIME)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No score found for matchId: " + matchId));

        Winner actualWinner = match.getWinner();

        for (var prediction : predictions) {
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
