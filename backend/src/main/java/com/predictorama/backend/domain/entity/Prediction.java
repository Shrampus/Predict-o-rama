package com.predictorama.backend.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
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
}
