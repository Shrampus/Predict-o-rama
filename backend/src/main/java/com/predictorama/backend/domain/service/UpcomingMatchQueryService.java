package com.predictorama.backend.domain.service;

import com.predictorama.backend.domain.port.persistence.MatchRepositoryPort;
import com.predictorama.backend.domain.service.result.UpcomingMatchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UpcomingMatchQueryService {

    private final MatchRepositoryPort matchRepositoryPort;

    public List<UpcomingMatchResult> getGenericUpcomingMatches() {
        Instant now = Instant.now();
        Instant end = now.plus(28, ChronoUnit.DAYS);

        return matchRepositoryPort.findByKickoffTimeBetween(now, end).stream()
                .map(match -> UpcomingMatchResult.builder()
                        .match(match)
                        .userGroups(List.of())
                        .build())
                .toList();
    }
}
