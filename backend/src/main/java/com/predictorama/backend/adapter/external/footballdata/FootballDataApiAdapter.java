package com.predictorama.backend.adapter.external.footballdata;

import com.predictorama.backend.domain.entity.Match;
import com.predictorama.backend.domain.entity.Team;
import com.predictorama.backend.domain.port.external.FootballDataPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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

        FootballDataMatchesResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/competitions/{competition}/matches")
                        .queryParam("dateFrom", dateFrom)
                        .queryParam("dateTo", dateTo)
                        .queryParam("status", "SCHEDULED")
                        .build(competition))
                .header("X-Auth-Token", apiKey)
                .retrieve()
                .body(FootballDataMatchesResponse.class);

        if (response == null || response.getMatches() == null) {
            return List.of();
        }

        return response.getMatches().stream()
                .map(this::toDomainMatch)
                .toList();
    }

    private Match toDomainMatch(FootballDataMatchResponse matchResponse) {
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

    private Match.MatchStatus mapStatus(String status) {
        return switch (status) {
            case "TIMED", "SCHEDULED" -> Match.MatchStatus.SCHEDULED;
            case "IN_PLAY", "PAUSED", "LIVE" -> Match.MatchStatus.LIVE;
            case "FINISHED" -> Match.MatchStatus.COMPLETED;
            case "CANCELLED", "POSTPONED", "SUSPENDED" -> Match.MatchStatus.CANCELLED;
            default -> Match.MatchStatus.SCHEDULED;
        };
    }
}