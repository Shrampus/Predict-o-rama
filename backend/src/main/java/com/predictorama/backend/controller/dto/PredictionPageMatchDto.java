package com.predictorama.backend.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class PredictionPageMatchDto {

    private UUID matchId;
    private String externalMatchId;

    private String homeTeamName;
    private String awayTeamName;

    private String homeTeamImage;
    private String awayTeamImage;

    private Instant kickoffTime;
    private String matchStatus;
}