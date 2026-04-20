package com.predictorama.backend.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class Prediction {
    private UUID id;
    private UUID userId;
    private UUID matchId;
    private UUID groupId;
    private List<Score> predictedScores;
    private Winner predictedWinner;
    private Instant submittedAt;
    private Integer result;

    public Optional<Score> primaryPredictedScore() {
        if (predictedScores == null || predictedScores.isEmpty()) {
            return Optional.empty();
        }

        return predictedScores.stream()
                .min(Comparator.comparingInt(score -> scorePriority(score.getScoreType())));
    }

    public Score requirePrimaryPredictedScore() {
        return primaryPredictedScore()
                .orElseThrow(() -> new IllegalStateException(
                        "Prediction " + id + " for match " + matchId + " does not contain any predicted scores."
                ));
    }

    private int scorePriority(Score.ScoreType scoreType) {
        if (scoreType == null) {
            return 99;
        }

        return switch (scoreType) {
            case NORMAL_TIME -> 0;
            case FULL_TIME -> 1;
            case PENALTIES -> 2;
        };
    }
}