package com.predictorama.backend.adapter.external.footballdata;

import com.predictorama.backend.adapter.external.footballdata.mapper.FootballDataMatchMapper;
import com.predictorama.backend.domain.entity.Match;
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
                    .filter(match -> {
                        String status = match.getStatus();
                        return "SCHEDULED".equals(status) || "TIMED".equals(status);
                    })
                    .filter(match -> hasValidUtcDate(match, competition))
                    .filter(match -> isFutureKickoff(match))
                    .filter(match -> hasResolvedTeams(match, competition))
                    .map(FootballDataMatchMapper::toDomainMatch)
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

    private boolean hasValidUtcDate(FootballDataMatchResponse match, String competition) {
        if (match == null) {
            log.warn("Skipping football-data match for competition={} because matchResponse is null", competition);
            return false;
        }

        if (match.getId() == null) {
            log.warn("Skipping football-data match for competition={} because id is null", competition);
            return false;
        }

        if (match.getUtcDate() == null || match.getUtcDate().isBlank()) {
            log.warn(
                    "Skipping football-data match for competition={} externalId={} because utcDate is missing",
                    competition,
                    match.getId()
            );
            return false;
        }

        try {
            Instant.parse(match.getUtcDate());
            return true;
        } catch (DateTimeParseException e) {
            log.warn(
                    "Skipping football-data match for competition={} externalId={} because utcDate is invalid value={}",
                    competition,
                    match.getId(),
                    match.getUtcDate()
            );
            return false;
        }
    }

    private boolean isFutureKickoff(FootballDataMatchResponse match) {
        return Instant.parse(match.getUtcDate()).isAfter(Instant.now());
    }

    private boolean hasResolvedTeams(FootballDataMatchResponse match, String competition) {
        if (match.getHomeTeam() == null) {
            log.warn(
                    "Skipping football-data match for competition={} externalId={} because homeTeam is missing",
                    competition,
                    match.getId()
            );
            return false;
        }

        if (match.getAwayTeam() == null) {
            log.warn(
                    "Skipping football-data match for competition={} externalId={} because awayTeam is missing",
                    competition,
                    match.getId()
            );
            return false;
        }

        String homeTeamName = normalize(match.getHomeTeam().getName());
        String awayTeamName = normalize(match.getAwayTeam().getName());

        if (homeTeamName == null) {
            log.warn(
                    "Skipping football-data match for competition={} externalId={} because homeTeam.name is missing",
                    competition,
                    match.getId()
            );
            return false;
        }

        if (awayTeamName == null) {
            log.warn(
                    "Skipping football-data match for competition={} externalId={} because awayTeam.name is missing",
                    competition,
                    match.getId()
            );
            return false;
        }

        return true;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}