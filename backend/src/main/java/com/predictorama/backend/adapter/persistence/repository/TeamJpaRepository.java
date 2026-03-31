package com.predictorama.backend.adapter.persistence.repository;

import com.predictorama.backend.adapter.persistence.entity.TeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TeamJpaRepository extends JpaRepository<TeamEntity, UUID> {

    Optional<TeamEntity> findByName(String name);
}
