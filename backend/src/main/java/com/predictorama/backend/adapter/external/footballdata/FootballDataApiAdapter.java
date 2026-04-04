package com.predictorama.backend.adapter.external.footballdata;

import com.predictorama.backend.domain.entity.Match;
import com.predictorama.backend.domain.entity.Team;
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
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

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
                            .queryParam("status", "SCHEDULED")
                            .build(competition))
                    .header("X-Auth-Token", apiKey)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, clientResponse) -> {
                        throw new FootballDataApiException(
                                "Football-data client error: HTTP " + clientResponse.getStatusCode().value()
                                        + " for competition=" + competition
                        );
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, clientResponse) -> {
                        throw new FootballDataApiException(
                                "Football-data server error: HTTP " + clientResponse.getStatusCode().value()
                                        + " for competition=" + competition
                        );
                    })
                    .body(FootballDataMatchesResponse.class);

            if (response == null || response.getMatches() == null) {
                log.warn("Football-data returned empty response for competition={}", competition);
                return List.of();
            }

            return response.getMatches().stream()
                    .flatMap(matchResponse -> toDomainMatch(matchResponse, competition).stream())
                    .toList();

        } catch (FootballDataApiException e) {
            log.warn("Football-data API request failed for competition={}: {}", competition, e.getMessage());
            return List.of();

        } catch (RestClientResponseException e) {
            log.warn(
                    "Football-data HTTP error for competition={}: status={} body={}",
                    competition,
                    e.getStatusCode().value(),
                    e.getResponseBodyAsString()
            );
            return List.of();

        } catch (RestClientException e) {
            log.error("Football-data transport error for competition={}: {}", competition, e.getMessage(), e);
            return List.of();

        } catch (Exception e) {
            log.error("Unexpected football-data adapter error for competition={}", competition, e);
            return List.of();
        }
    }

    private List<Match> toDomainMatch(FootballDataMatchResponse matchResponse, String competition) {
        if (matchResponse == null) {
            log.warn("Skipping football-data match for competition={} because matchResponse is null", competition);
            return List.of();
        }

        if (matchResponse.getId() == null) {
            log.warn("Skipping football-data match for competition={} because id is null", competition);
            return List.of();
        }

        if (matchResponse.getUtcDate() == null || matchResponse.getUtcDate().isBlank()) {
            log.warn(
                    "Skipping football-data match for competition={} externalId={} because utcDate is missing",
                    competition,
                    matchResponse.getId()
            );
            return List.of();
        }

        if (matchResponse.getHomeTeam() == null) {
            log.warn(
                    "Skipping football-data match for competition={} externalId={} because homeTeam is missing",
                    competition,
                    matchResponse.getId()
            );
            return List.of();
        }

        if (matchResponse.getAwayTeam() == null) {
            log.warn(
                    "Skipping football-data match for competition={} externalId={} because awayTeam is missing",
                    competition,
                    matchResponse.getId()
            );
            return List.of();
        }

        String homeTeamName = normalize(matchResponse.getHomeTeam().getName());
        String awayTeamName = normalize(matchResponse.getAwayTeam().getName());

        if (homeTeamName == null) {
            log.warn(
                    "Skipping football-data match for competition={} externalId={} because homeTeam.name is missing",
                    competition,
                    matchResponse.getId()
            );
            return List.of();
        }

        if (awayTeamName == null) {
            log.warn(
                    "Skipping football-data match for competition={} externalId={} because awayTeam.name is missing",
                    competition,
                    matchResponse.getId()
            );
            return List.of();
        }

        Instant kickoffTime;
        try {
            kickoffTime = Instant.parse(matchResponse.getUtcDate());
        } catch (DateTimeParseException e) {
            log.warn(
                    "Skipping football-data match for competition={} externalId={} because utcDate is invalid value={}",
                    competition,
                    matchResponse.getId(),
                    matchResponse.getUtcDate()
            );
            return List.of();
        }

        Match match = Match.builder()
                .id(UUID.randomUUID()) // temporary until persisted
                .tournamentId(null)
                .name(homeTeamName + " vs " + awayTeamName)
                .description(null)
                .homeTeam(Team.builder()
                        .id(UUID.randomUUID())
                        .name(homeTeamName)
                        .imageUrl(normalize(matchResponse.getHomeTeam().getCrest()))
                        .build())
                .awayTeam(Team.builder()
                        .id(UUID.randomUUID())
                        .name(awayTeamName)
                        .imageUrl(normalize(matchResponse.getAwayTeam().getCrest()))
                        .build())
                .matchStatus(mapStatus(matchResponse.getStatus()))
                .kickoffTime(kickoffTime)
                .scores(List.of())
                .winner(null)
                .externalId(String.valueOf(matchResponse.getId()))
                .build();

        return List.of(match);
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

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}