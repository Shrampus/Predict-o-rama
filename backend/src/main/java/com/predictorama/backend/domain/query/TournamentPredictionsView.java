package com.predictorama.backend.domain.query;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class TournamentPredictionsView {
    private String tournamentName;
    private String seasonLabel;
    private String phaseLabel;
    private List<TournamentMatchPredictionView> matches;
}