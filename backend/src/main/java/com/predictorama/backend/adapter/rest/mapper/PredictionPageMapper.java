package com.predictorama.backend.adapter.rest.mapper;

import com.predictorama.backend.adapter.rest.dto.PredictionPageMatchDto;
import com.predictorama.backend.domain.entity.Match;
import com.predictorama.backend.domain.entity.Prediction;
import com.predictorama.backend.domain.entity.Score;

public class PredictionPageMapper {

    public static PredictionPageMatchDto toDto(Match match, Prediction prediction) {
        Score predictedScore = getPrimaryPredictedScore(prediction);

        return PredictionPageMatchDto.builder()
                .matchId(match.getId())
                .externalMatchId(match.getExternalId())
                .homeTeamName(match.getHomeTeam().getName())
                .awayTeamName(match.getAwayTeam().getName())
                .homeTeamImage(match.getHomeTeam().getImageUrl())
                .awayTeamImage(match.getAwayTeam().getImageUrl())
                .kickoffTime(match.getKickoffTime())
                .matchStatus(match.getMatchStatus().name())
                .predictionId(prediction != null ? prediction.getId() : null)
                .predictedHomeScore(predictedScore != null ? predictedScore.getHomeScore() : null)
                .predictedAwayScore(predictedScore != null ? predictedScore.getAwayScore() : null)
                .predictedWinner(prediction != null && prediction.getPredictedWinner() != null
                        ? prediction.getPredictedWinner().name()
                        : null)
                .build();
    }

    private static Score getPrimaryPredictedScore(Prediction prediction) {
        if (prediction == null || prediction.getPredictedScores() == null || prediction.getPredictedScores().isEmpty()) {
            return null;
        }

        return prediction.getPredictedScores().get(0);
    }
}