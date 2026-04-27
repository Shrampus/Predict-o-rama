package com.predictorama.backend.domain.port.persistence;

import com.predictorama.backend.domain.entity.Ruleset;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface RulesetRepositoryPort {

    Optional<Ruleset> findByGroupIdAndTournamentId(UUID groupId, UUID tournamentId);

    Ruleset upsertForGroupTournament(UUID groupId, UUID tournamentId, Map<String, Integer> rulePoints);

}
