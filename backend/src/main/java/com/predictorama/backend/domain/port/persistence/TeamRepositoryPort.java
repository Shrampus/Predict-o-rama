package com.predictorama.backend.domain.port.persistence;

import com.predictorama.backend.domain.entity.Team;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamRepositoryPort {

    Team save(Team team);

    Optional<Team> findById(UUID id);

    List<Team> findAll();

    Optional<Team> findByName(String name);
}
