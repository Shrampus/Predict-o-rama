
package com.predictorama.backend.adapter.persistence.mapper;

import com.predictorama.backend.adapter.persistence.entity.PredictionEntity;
import com.predictorama.backend.domain.entity.Prediction;
import com.predictorama.backend.domain.entity.Score;
import com.predictorama.backend.domain.entity.Winner;

import java.util.List;

public class PredictionMapper {

    public static Prediction toDomain(PredictionEntity entity, List<Score> predictedScores) {
        return Prediction.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .matchId(entity.getMatchId())
                .groupId(entity.getGroupId())
                .predictedScores(predictedScores)
                .predictedWinner(entity.getPredictedWinner())
                .submittedAt(entity.getSubmittedAt())
                .result(entity.getResult())
                .build();
    }

    public static PredictionEntity toEntity(Prediction prediction) {
        return PredictionEntity.builder()
                .id(prediction.getId())
                .userId(prediction.getUserId())
                .matchId(prediction.getMatchId())
                .groupId(prediction.getGroupId())
                .predictedWinner(prediction.getPredictedWinner())
                .submittedAt(prediction.getSubmittedAt())
                .result(prediction.getResult())
                .build();
    }
}
