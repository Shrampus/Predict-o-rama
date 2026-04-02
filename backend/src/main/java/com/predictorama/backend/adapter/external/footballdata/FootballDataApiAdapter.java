package com.predictorama.backend.adapter.external.footballdata;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;

@Component
public class FootballDataApiAdapter {

    private final RestClient restClient;

    @Value("${football-data.api-key}")
    private String apiKey;

    public FootballDataApiAdapter(@Value("${football-data.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public FootballDataMatchesResponse getUpcomingMatches(String competition) {
        LocalDate dateFrom = LocalDate.now();
        LocalDate dateTo = dateFrom.plusDays(28);

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/competitions/{competition}/matches")
                        .queryParam("dateFrom", dateFrom)
                        .queryParam("dateTo", dateTo)
                        .queryParam("status", "SCHEDULED")
                        .build(competition))
                .header("X-Auth-Token", apiKey)
                .retrieve()
                .body(FootballDataMatchesResponse.class);
    }
}