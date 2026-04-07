package com.predictorama.backend.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Score {
    private Integer homeScore;
    private Integer awayScore;
    private ScoreType scoreType;

    public int getScore() {
        return (homeScore != null ? homeScore : 0) + (awayScore != null ? awayScore : 0);
    }

    public Score(int homeScore, int awayScore) {
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.scoreType = ScoreType.NORMAL_TIME;
    }

    public enum ScoreType{
        NORMAL_TIME, FULL_TIME, PENALTIES
    }
}
