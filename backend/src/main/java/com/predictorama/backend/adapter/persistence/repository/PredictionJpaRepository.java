package com.predictorama.backend.adapter.persistence.repository;

import com.predictorama.backend.adapter.persistence.entity.PredictionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PredictionJpaRepository extends JpaRepository<PredictionEntity, UUID> {

    Optional<PredictionEntity> findByUserIdAndMatchIdAndGroupId(UUID userId, UUID matchId, UUID groupId);

    List<PredictionEntity> findByMatchIdAndGroupId(UUID matchId, UUID groupId);

    List<PredictionEntity> findByUserId(UUID userId);

    List<PredictionEntity> findByGroupId(UUID groupId);
}
