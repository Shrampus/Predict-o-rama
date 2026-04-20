package com.predictorama.backend.adapter.persistence.mapper;

import com.predictorama.backend.adapter.persistence.entity.MatchEntity;
import com.predictorama.backend.domain.entity.Match;
import com.predictorama.backend.domain.entity.Score;
import com.predictorama.backend.domain.entity.Team;

import java.util.List;

public class MatchMapper {

    public static Match toDomain(MatchEntity entity, List<Score> scores, Team homeTeam, Team awayTeam) {
        return Match.builder()
                .id(entity.getId())
                .tournamentId(entity.getTournamentId())
                .name(entity.getName())
                .description(entity.getDescription())
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .matchStatus(entity.getMatchStatus())
                .kickoffTime(entity.getKickoffTime())
                .scores(scores)
                .winner(entity.getWinner())
                .externalId(entity.getExternalId())
                .build();
    }

    public static MatchEntity toEntity(Match match) {
        return MatchEntity.builder()
                .id(match.getId())
                .tournamentId(match.getTournamentId())
                .name(match.getName())
                .description(match.getDescription())
                .homeTeamId(match.getHomeTeam().getId())
                .awayTeamId(match.getAwayTeam().getId())
                .matchStatus(match.getMatchStatus())
                .kickoffTime(match.getKickoffTime())
                .winner(match.getWinner())
                .externalId(match.getExternalId())
                .build();
    }
}
