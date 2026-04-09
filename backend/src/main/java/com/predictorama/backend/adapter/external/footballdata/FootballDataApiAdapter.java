package com.predictorama.backend.adapter.external.footballdata;

import com.predictorama.backend.domain.entity.Match;
import com.predictorama.backend.domain.entity.Score;
import com.predictorama.backend.domain.entity.Team;
import com.predictorama.backend.domain.entity.Winner;
import com.predictorama.backend.domain.port.external.FootballDataPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class FootballDataApiAdapter implements FootballDataPort {

    private final RestClient restClient;

    @Value("${football-data.api-key}")
    private String apiKey;

    public FootballDataApiAdapter(@Value("${football-data.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public List<Match> getUpcomingMatches(String competition) {
        LocalDate dateFrom = LocalDate.now();
        LocalDate dateTo = dateFrom.plusDays(28);

        try {
            FootballDataMatchesResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/competitions/{competition}/matches")
                            .queryParam("dateFrom", dateFrom)
                            .queryParam("dateTo", dateTo)
                            .build(competition))
                    .header("X-Auth-Token", apiKey)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, clientResponse) -> {
                        throw new FootballDataApiException(
                                "Football-data client error: HTTP " + clientResponse.getStatusCode().value()
                                        + " for competition=" + competition);
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, clientResponse) -> {
                        throw new FootballDataApiException(
                                "Football-data server error: HTTP " + clientResponse.getStatusCode().value()
                                        + " for competition=" + competition);
                    })
                    .body(FootballDataMatchesResponse.class);

            if (response == null || response.getMatches() == null) {
                log.warn("Football-data returned empty response for competition={}", competition);
                return List.of();
            }

            return response.getMatches().stream()
                    .filter(match -> {
                        String status = match.getStatus();
                        return "SCHEDULED".equals(status) || "TIMED".equals(status);
                    })
                    .filter(match -> Instant.parse(match.getUtcDate()).isAfter(Instant.now()))
                    .map(this::toDomainMatch)
                    .toList();

        } catch (FootballDataApiException e) {
            log.warn("Football-data API request failed for competition={}: {}", competition, e.getMessage());
            return List.of();

        } catch (RestClientResponseException e) {
            log.warn(
                    "Football-data HTTP error for competition={}: status={} body={}",
                    competition,
                    e.getStatusCode().value(),
                    e.getResponseBodyAsString());
            return List.of();

        } catch (RestClientException e) {
            log.error("Football-data transport error for competition={}: {}", competition, e.getMessage(), e);
            return List.of();

        } catch (Exception e) {
            log.error("Unexpected football-data adapter error for competition={}", competition, e);
            return List.of();
        }
    }

    @Override
    public List<Match> getFinishedMatches(String competition) {
        LocalDate dateFrom = LocalDate.now().minusDays(28);
        LocalDate dateTo = LocalDate.now();

        try {
            FootballDataMatchesResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/competitions/{competition}/matches")
                            .queryParam("dateFrom", dateFrom)
                            .queryParam("dateTo", dateTo)
                            .build(competition))
                    .header("X-Auth-Token", apiKey)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, clientResponse) -> {
                        throw new FootballDataApiException(
                                "Football-data client error: HTTP " + clientResponse.getStatusCode().value()
                                        + " for competition=" + competition);
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, clientResponse) -> {
                        throw new FootballDataApiException(
                                "Football-data server error: HTTP " + clientResponse.getStatusCode().value()
                                        + " for competition=" + competition);
                    })
                    .body(FootballDataMatchesResponse.class);

            if (response == null || response.getMatches() == null) {
                log.warn("Football-data returned empty response for competition={}", competition);
                return List.of();
            }

            return response.getMatches().stream()
                    .filter(match -> "FINISHED".equals(match.getStatus()))
                    .map(this::toDomainMatch)
                    .toList();

        } catch (FootballDataApiException e) {
            log.warn("Football-data API request failed for competition={}: {}", competition, e.getMessage());
            return List.of();

        } catch (RestClientResponseException e) {
            log.warn(
                    "Football-data HTTP error for competition={}: status={} body={}",
                    competition,
                    e.getStatusCode().value(),
                    e.getResponseBodyAsString());
            return List.of();

        } catch (RestClientException e) {
            log.error("Football-data transport error for competition={}: {}", competition, e.getMessage(), e);
            return List.of();

        } catch (Exception e) {
            log.error("Unexpected football-data adapter error for competition={}", competition, e);
            return List.of();
        }
    }

    private Match toDomainMatch(FootballDataMatchResponse matchResponse) {

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

    private Match.MatchStatus mapStatus(String status) {
        return switch (status) {
            case "TIMED", "SCHEDULED" -> Match.MatchStatus.SCHEDULED;
            case "IN_PLAY", "PAUSED", "LIVE" -> Match.MatchStatus.LIVE;
            case "FINISHED" -> Match.MatchStatus.COMPLETED;
            case "CANCELLED", "POSTPONED", "SUSPENDED" -> Match.MatchStatus.CANCELLED;
            default -> Match.MatchStatus.SCHEDULED;
        };
    }

    private Winner mapWinner(String winner) {
        if (winner == null) return null;
        return switch (winner) {
            case "HOME_TEAM" -> Winner.HOME;
            case "AWAY_TEAM" -> Winner.AWAY;
            case "DRAW" -> Winner.DRAW;
            default -> null;
        };
    }
}