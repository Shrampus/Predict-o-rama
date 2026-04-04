package com.predictorama.backend.adapter.persistence.repository;

import com.predictorama.backend.adapter.persistence.entity.PredictionScoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PredictionScoreJpaRepository extends JpaRepository<PredictionScoreEntity, UUID> {

    List<PredictionScoreEntity> findByPredictionId(UUID predictionId);

    void deleteByPredictionId(UUID predictionId);
}
