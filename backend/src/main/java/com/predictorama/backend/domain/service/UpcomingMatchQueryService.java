package com.predictorama.backend.domain.service;

import com.predictorama.backend.domain.entity.Match;
import com.predictorama.backend.domain.entity.Tournament;
import com.predictorama.backend.domain.port.persistence.MatchRepositoryPort;
import com.predictorama.backend.domain.port.persistence.TournamentRepositoryPort;
import com.predictorama.backend.domain.service.result.UpcomingMatchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UpcomingMatchQueryService {

    private final MatchRepositoryPort matchRepositoryPort;
    private final TournamentRepositoryPort tournamentRepositoryPort;

    public List<UpcomingMatchResult> getGenericUpcomingMatches() {
        Instant now = Instant.now();
        Instant end = now.plus(28, ChronoUnit.DAYS);

        List<Match> matches = matchRepositoryPort.findByKickoffTimeBetween(now, end);

        Set<UUID> tournamentIds = matches.stream().map(Match::getTournamentId).collect(Collectors.toSet());
        Map<UUID, Tournament> tournamentsById = tournamentRepositoryPort.findAllById(tournamentIds).stream()
                .collect(Collectors.toMap(Tournament::getId, t -> t));

        return matches.stream()
                .map(match -> {
                    Tournament tournament = tournamentsById.get(match.getTournamentId());
                    return UpcomingMatchResult.builder()
                            .match(match)
                            .tournamentName(tournament != null ? tournament.getName() : null)
                            .userGroups(List.of())
                            .build();
                })
                .toList();
    }
}
