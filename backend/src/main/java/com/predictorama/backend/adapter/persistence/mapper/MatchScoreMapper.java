package com.predictorama.backend.adapter.persistence.mapper;

import com.predictorama.backend.adapter.persistence.entity.MatchScoreEntity;
import com.predictorama.backend.domain.entity.Score;

import java.util.UUID;

public class MatchScoreMapper {

    public static Score toDomain(MatchScoreEntity entity) {
        return Score.builder()
                .homeScore(entity.getHomeScore())
                .awayScore(entity.getAwayScore())
                .scoreType(entity.getScoreType())
                .build();
    }

    public static MatchScoreEntity toEntity(UUID matchId, Score score) {
        return MatchScoreEntity.builder()
                .id(UUID.randomUUID())
                .matchId(matchId)
                .scoreType(score.getScoreType())
                .homeScore(score.getHomeScore())
                .awayScore(score.getAwayScore())
                .build();
    }
}
