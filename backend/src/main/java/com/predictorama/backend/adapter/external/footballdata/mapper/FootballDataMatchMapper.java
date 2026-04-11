package com.predictorama.backend.adapter.external.footballdata.mapper;

import com.predictorama.backend.domain.entity.Match;
import com.predictorama.backend.domain.entity.Team;
import com.predictorama.backend.adapter.external.footballdata.FootballDataMatchResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class FootballDataMatchMapper {

    private FootballDataMatchMapper() {
    }

    public static Match toDomainMatch(FootballDataMatchResponse matchResponse) {
        return Match.builder()
                .id(UUID.randomUUID()) // temporary until persisted
                .tournamentId(null)
                .name(matchResponse.getHomeTeam().getName() + " vs " + matchResponse.getAwayTeam().getName())
                .description(null)
                .homeTeam(Team.builder()
                        .id(UUID.randomUUID())
                        .name(matchResponse.getHomeTeam().getName())
                        .imageUrl(matchResponse.getHomeTeam().getCrest())
                        .build())
                .awayTeam(Team.builder()
                        .id(UUID.randomUUID())
                        .name(matchResponse.getAwayTeam().getName())
                        .imageUrl(matchResponse.getAwayTeam().getCrest())
                        .build())
                .matchStatus(mapStatus(matchResponse.getStatus()))
                .kickoffTime(Instant.parse(matchResponse.getUtcDate()))
                .scores(List.of())
                .winner(null)
                .externalId(String.valueOf(matchResponse.getId()))
                .build();
    }

    private static Match.MatchStatus mapStatus(String status) {
        return switch (status) {
            case "TIMED", "SCHEDULED" -> Match.MatchStatus.SCHEDULED;
            case "IN_PLAY", "PAUSED", "LIVE" -> Match.MatchStatus.LIVE;
            case "FINISHED" -> Match.MatchStatus.COMPLETED;
            case "CANCELLED", "POSTPONED", "SUSPENDED" -> Match.MatchStatus.CANCELLED;
            default -> Match.MatchStatus.SCHEDULED;
        };
    }
}