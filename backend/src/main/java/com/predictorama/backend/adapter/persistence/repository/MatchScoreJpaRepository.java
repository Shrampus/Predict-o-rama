package com.predictorama.backend.adapter.persistence.repository;

import com.predictorama.backend.adapter.persistence.entity.MatchScoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MatchScoreJpaRepository extends JpaRepository<MatchScoreEntity, UUID> {

    List<MatchScoreEntity> findByMatchId(UUID matchId);

    void deleteByMatchId(UUID matchId);
}
