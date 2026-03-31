package com.predictorama.backend.domain.service;

import com.predictorama.backend.adapter.external.footballdata.FootballDataApiAdapter;
import com.predictorama.backend.controller.dto.PredictionPageMatchDto;
import com.predictorama.backend.controller.dto.PredictionPageResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PredictionPageService {

    private final FootballDataApiAdapter footballDataApiAdapter;

    public PredictionPageResponseDto getPredictionPage(String competition) {

        var response = footballDataApiAdapter.getUpcomingMatches(competition);

        var matches = response.getMatches().stream()
                .map(m -> PredictionPageMatchDto.builder()
                        .matchId(UUID.randomUUID()) // temporary until DB is used
                        .externalMatchId(String.valueOf(m.getId()))
                        .homeTeamName(m.getHomeTeam().getName())
                        .awayTeamName(m.getAwayTeam().getName())
                        .homeTeamImage(m.getHomeTeam().getCrest())
                        .awayTeamImage(m.getAwayTeam().getCrest())
                        .kickoffTime(Instant.parse(m.getUtcDate()))
                        .matchStatus(m.getStatus())
                        .build())
                .toList();

        return PredictionPageResponseDto.builder()
                .matches(matches)
                .build();
    }
}