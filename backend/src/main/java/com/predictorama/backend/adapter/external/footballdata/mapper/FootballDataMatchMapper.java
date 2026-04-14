package com.predictorama.backend.adapter.external.footballdata.mapper;

import com.predictorama.backend.adapter.external.footballdata.FootballDataMatchResponse;
import com.predictorama.backend.domain.entity.Match;
import com.predictorama.backend.domain.entity.Score;
import com.predictorama.backend.domain.entity.Team;
import com.predictorama.backend.domain.entity.Winner;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class FootballDataMatchMapper {

    private FootballDataMatchMapper() {
    }

    public static Match toDomainMatch(FootballDataMatchResponse matchResponse) {
        var scoreResponse = matchResponse.getScore();
        List<Score> scores = List.of();
        Winner winner = null;

        if (scoreResponse != null && scoreResponse.getFullTime() != null) {
            scores = List.of(Score.builder()
                    .homeScore(scoreResponse.getFullTime().getHome())
                    .awayScore(scoreResponse.getFullTime().getAway())
                    .scoreType(Score.ScoreType.FULL_TIME)
                    .build());
            winner = mapWinner(scoreResponse.getWinner());
        }

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
                .scores(scores)
                .winner(winner)
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

    private static Winner mapWinner(String winner) {
        if (winner == null) return null;
        return switch (winner) {
            case "HOME_TEAM" -> Winner.HOME;
            case "AWAY_TEAM" -> Winner.AWAY;
            case "DRAW" -> Winner.DRAW;
            default -> null;
        };
    }
}