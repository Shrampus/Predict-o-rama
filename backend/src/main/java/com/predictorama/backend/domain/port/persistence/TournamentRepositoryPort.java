package com.predictorama.backend.domain.port.persistence;

import com.predictorama.backend.domain.entity.Tournament;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TournamentRepositoryPort {

    Tournament save(Tournament tournament);

    Optional<Tournament> findById(UUID id);
    Optional<Tournament> findByNameIgnoreCase(String name);

    List<Tournament> findAll();
}
