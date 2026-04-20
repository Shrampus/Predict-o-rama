package com.predictorama.backend.adapter.persistence.mapper;

import com.predictorama.backend.adapter.persistence.entity.PredictionScoreEntity;
import com.predictorama.backend.domain.entity.Score;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PredictionScoreMapper {

    public static Score toDomain(PredictionScoreEntity entity) {
        return Score.builder()
                .homeScore(entity.getHomeScore())
                .awayScore(entity.getAwayScore())
                .scoreType(entity.getScoreType())
                .build();
    }

    public static PredictionScoreEntity toEntity(UUID predictionId, Score score) {
        return PredictionScoreEntity.builder()
                .id(UUID.randomUUID())
                .predictionId(predictionId)
                .scoreType(score.getScoreType())
                .homeScore(score.getHomeScore())
                .awayScore(score.getAwayScore())
                .build();
    }

    public static List<PredictionScoreEntity> toEntities(UUID predictionId, List<Score> scores) {
        if (scores == null || scores.isEmpty()) {
            return Collections.emptyList();
        }

        return scores.stream()
                .map(score -> toEntity(predictionId, score))
                .toList();
    }
}
