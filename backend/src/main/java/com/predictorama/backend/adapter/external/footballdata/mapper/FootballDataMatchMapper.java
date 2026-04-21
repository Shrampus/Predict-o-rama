package com.predictorama.backend.adapter.external.footballdata.mapper;

import com.predictorama.backend.adapter.external.footballdata.FootballDataMatchResponse;
import com.predictorama.backend.domain.entity.Match;
import com.predictorama.backend.domain.entity.Score;
import com.predictorama.backend.domain.entity.Team;
import com.predictorama.backend.domain.entity.Winner;

import java.time.Instant;
import java.util.List;

public final class FootballDataMatchMapper {

    private FootballDataMatchMapper() {
    }

    public static Match toDomainMatch(FootballDataMatchResponse matchResponse) {
        var scoreResponse = matchResponse.getScore();
        List<Score> scores = List.of();
        Winner winner = null;

        if (hasCompleteFullTimeScore(scoreResponse)) {
            scores = List.of(Score.builder()
                    .homeScore(scoreResponse.getFullTime().getHome())
                    .awayScore(scoreResponse.getFullTime().getAway())
                    .scoreType(Score.ScoreType.FULL_TIME)
                    .build());
            winner = mapWinner(scoreResponse.getWinner());
        }

        return Match.builder()
                .id(null)
                .tournamentId(null)
                .name(matchResponse.getHomeTeam().getName() + " vs " + matchResponse.getAwayTeam().getName())
                .description(null)
                .homeTeam(Team.builder()
                        .id(null)
                        .externalId(matchResponse.getHomeTeam().getId() != null
                                ? String.valueOf(matchResponse.getHomeTeam().getId())
                                : null)
                        .name(matchResponse.getHomeTeam().getName())
                        .imageUrl(matchResponse.getHomeTeam().getCrest())
                        .build())
                .awayTeam(Team.builder()
                        .id(null)
                        .externalId(matchResponse.getAwayTeam().getId() != null
                                ? String.valueOf(matchResponse.getAwayTeam().getId())
                                : null)
                        .name(matchResponse.getAwayTeam().getName())
                        .imageUrl(matchResponse.getAwayTeam().getCrest())
                        .build())
                .matchStatus(mapStatus(matchResponse.getStatus()))
                .kickoffTime(Instant.parse(matchResponse.getUtcDate()))
                .seasonIdentifier(extractSeasonIdentifier(matchResponse))
                .seasonLabel(extractSeasonLabel(matchResponse))
                .roundIdentifier(buildRoundIdentifier(matchResponse))
                .groupIdentifier(extractGroupIdentifier(matchResponse))
                .matchdayIdentifier(matchResponse.getMatchday())
                .scores(scores)
                .winner(winner)
                .externalId(String.valueOf(matchResponse.getId()))
                .build();
    }

    private static boolean hasCompleteFullTimeScore(com.predictorama.backend.adapter.external.footballdata.FootballDataScoreResponse scoreResponse) {
        if (scoreResponse == null || scoreResponse.getFullTime() == null) {
            return false;
        }

        return scoreResponse.getFullTime().getHome() != null
                && scoreResponse.getFullTime().getAway() != null;
    }

    public static String extractSeasonIdentifier(FootballDataMatchResponse matchResponse) {
        if (matchResponse == null || matchResponse.getSeason() == null || matchResponse.getSeason().getId() == null) {
            return null;
        }

        return String.valueOf(matchResponse.getSeason().getId());
    }

    public static String extractSeasonLabel(FootballDataMatchResponse matchResponse) {
        if (matchResponse == null || matchResponse.getSeason() == null) {
            return null;
        }

        String startDate = normalize(matchResponse.getSeason().getStartDate());
        String endDate = normalize(matchResponse.getSeason().getEndDate());

        if (startDate == null || endDate == null || startDate.length() < 4 || endDate.length() < 4) {
            return null;
        }

        String startYear = startDate.substring(0, 4);
        String endYear = endDate.substring(0, 4);

        if (startYear.equals(endYear)) {
            return startYear;
        }

        return startYear + "/" + endYear.substring(2);
    }

    private static String buildRoundIdentifier(FootballDataMatchResponse matchResponse) {
        String stage = normalize(matchResponse.getStage());
        String humanizedGroup = extractGroupIdentifier(matchResponse);
        Integer matchday = matchResponse.getMatchday();

        if (humanizedGroup != null && matchday != null) {
            return humanizedGroup + " - Matchday " + matchday;
        }

        if (humanizedGroup != null) {
            return humanizedGroup;
        }

        if (stage != null && matchday != null) {
            return humanize(stage) + " - Matchday " + matchday;
        }

        if (matchday != null) {
            return "Matchday " + matchday;
        }

        if (stage != null) {
            return humanize(stage);
        }

        return null;
    }

    public static String extractGroupIdentifier(FootballDataMatchResponse matchResponse) {
        String group = normalize(matchResponse.getGroup());
        return group != null ? humanize(group) : null;
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

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private static String humanize(String value) {
        return value.replace('_', ' ').trim();
    }
}
