package com.predictorama.backend.adapter.persistence.repository;

import com.predictorama.backend.adapter.persistence.entity.MatchEntity;
import com.predictorama.backend.domain.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatchJpaRepository extends JpaRepository<MatchEntity, UUID> {

    List<MatchEntity> findByTournamentId(UUID tournamentId);

    List<MatchEntity> findByTournamentIdAndMatchStatus(UUID tournamentId, Match.MatchStatus matchStatus);

    Optional<MatchEntity> findByExternalId(String externalId);
}
