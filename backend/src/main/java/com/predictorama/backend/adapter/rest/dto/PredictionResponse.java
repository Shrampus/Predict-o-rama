package com.predictorama.backend.adapter.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

import com.predictorama.backend.domain.entity.Winner;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PredictionResponse {

    private UUID predictionId;
    private UUID matchId;
    private Integer homeScore;
    private Integer awayScore;
    private Winner predictedWinner;
    private Instant submittedAt;
}