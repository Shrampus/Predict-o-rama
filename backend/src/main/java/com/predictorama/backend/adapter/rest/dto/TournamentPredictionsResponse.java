package com.predictorama.backend.adapter.rest.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TournamentPredictionsResponse {
    private String tournamentName;
    private List<TournamentMatchPredictionDto> matches;
}