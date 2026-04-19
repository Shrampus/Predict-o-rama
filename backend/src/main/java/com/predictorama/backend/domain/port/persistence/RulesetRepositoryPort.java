package com.predictorama.backend.domain.port.persistence;

import com.predictorama.backend.domain.entity.Ruleset;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RulesetRepositoryPort {

    Ruleset save(Ruleset ruleset);

    Optional<Ruleset> findById(UUID id);

    Optional<Ruleset> findByName(String name);

    List<Ruleset> findAll();
}
