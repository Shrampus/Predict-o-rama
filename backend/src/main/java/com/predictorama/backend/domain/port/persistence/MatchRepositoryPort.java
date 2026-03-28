package com.predictorama.backend.domain.port.persistence;

import com.predictorama.backend.domain.entity.Match;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatchRepositoryPort {

    Match save(Match match);

    Optional<Match> findById(UUID id);

    List<Match> findByTournamentId(UUID tournamentId);

    List<Match> findByTournamentIdAndMatchStatus(UUID tournamentId, Match.MatchStatus matchStatus);

    Optional<Match> findByExternalId(String externalId);
}
