package com.predictorama.backend.adapter.rest.dto;

import com.predictorama.backend.domain.entity.Winner;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePredictionRequest {

    @NotNull
    private UUID groupId;

    @NotNull
    private UUID matchId;

    @Min(0)
    private int homeScore;

    @Min(0)
    private int awayScore;

    @NotNull
    private Winner predictedWinner;
}