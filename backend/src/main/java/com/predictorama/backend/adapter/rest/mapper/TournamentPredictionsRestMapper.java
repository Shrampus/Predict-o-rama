package com.predictorama.backend.adapter.rest.mapper;

import com.predictorama.backend.adapter.rest.dto.TournamentMatchPredictionDto;
import com.predictorama.backend.adapter.rest.dto.TournamentPredictionsResponse;
import com.predictorama.backend.domain.query.TournamentMatchPredictionView;
import com.predictorama.backend.domain.query.TournamentPredictionsView;

public final class TournamentPredictionsRestMapper {

    private TournamentPredictionsRestMapper() {
    }

    public static TournamentPredictionsResponse toResponse(TournamentPredictionsView view) {
        return new TournamentPredictionsResponse(
                view.getTournamentName(),
                view.getMatches().stream()
                        .map(TournamentPredictionsRestMapper::toMatchDto)
                        .toList()
        );
    }

    private static TournamentMatchPredictionDto toMatchDto(TournamentMatchPredictionView view) {
        return TournamentMatchPredictionDto.builder()
                .matchId(view.getMatchId())
                .externalMatchId(view.getExternalMatchId())
                .homeTeamName(view.getHomeTeamName())
                .awayTeamName(view.getAwayTeamName())
                .homeTeamImage(view.getHomeTeamImage())
                .awayTeamImage(view.getAwayTeamImage())
                .kickoffTime(view.getKickoffTime())
                .matchStatus(view.getMatchStatus())
                .predictionId(view.getPredictionId())
                .predictedHomeScore(view.getPredictedHomeScore())
                .predictedAwayScore(view.getPredictedAwayScore())
                .predictedWinner(view.getPredictedWinner())
                .build();
    }
}