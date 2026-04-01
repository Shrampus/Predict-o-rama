package com.predictorama.backend.adapter.rest.mapper;

import com.predictorama.backend.adapter.rest.dto.PredictionPageMatchDto;
import com.predictorama.backend.domain.entity.Match;

public class PredictionPageMapper {

    public static PredictionPageMatchDto toDto(Match match) {
        return PredictionPageMatchDto.builder()
                .matchId(match.getId())
                .externalMatchId(match.getExternalId())
                .homeTeamName(match.getHomeTeam().getName())
                .awayTeamName(match.getAwayTeam().getName())
                .homeTeamImage(null)
                .awayTeamImage(null)
                .kickoffTime(match.getKickoffTime())
                .matchStatus(match.getMatchStatus().name())
                .build();
    }
}