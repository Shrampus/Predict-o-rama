package com.predictorama.backend.domain.port.persistence;

import com.predictorama.backend.domain.entity.Match;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatchRepositoryPort {

    Match save(Match match);

    Optional<Match> findById(UUID id);

    List<Match> findByTournamentId(UUID tournamentId);

    List<Match> findByTournamentIdAndMatchStatus(UUID tournamentId, Match.MatchStatus matchStatus);

    List<Match> findByTournamentIdAndKickoffTimeBetween(UUID tournamentId, Instant from, Instant to);

    List<Match> findByKickoffTimeBetween(Instant from, Instant to);

    Optional<Match> findByExternalId(String externalId);
}