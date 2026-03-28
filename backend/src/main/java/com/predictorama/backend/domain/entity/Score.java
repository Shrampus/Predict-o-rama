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

    private enum ScoreType{
        NORMAL_TIME, FULL_TIME
    }
}
