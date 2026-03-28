package com.predictorama.backend.adapter.persistence.repository;

import com.predictorama.backend.adapter.persistence.entity.TournamentEntity;
import com.predictorama.backend.domain.entity.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TournamentJpaRepository extends JpaRepository<TournamentEntity, UUID> {

    List<TournamentEntity> findBySport(Tournament.Sport sport);
}
