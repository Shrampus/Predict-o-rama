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
public class Match {
    private UUID id;
    private UUID tournamentId;
    private String name;
    private String description;
    private Team homeTeam;
    private Team awayTeam;
    private MatchStatus matchStatus;
    private Instant kickoffTime;
    private List<Score> scores;
    private Winner winner;
    private String externalId;

    public Optional<Score> primaryScore() {
        if (scores == null || scores.isEmpty()) {
            return Optional.empty();
        }
        return scores.stream()
                .min(Comparator.comparingInt(s -> scoreTypePriority(s.getScoreType())));
    }

    private int scoreTypePriority(Score.ScoreType scoreType) {
        if (scoreType == null) return 99;
        return switch (scoreType) {
            case NORMAL_TIME -> 0;
            case FULL_TIME -> 1;
            case PENALTIES -> 2;
        };
    }

    public enum MatchStatus{
        SCHEDULED, LIVE, COMPLETED, CANCELLED
    }
}
