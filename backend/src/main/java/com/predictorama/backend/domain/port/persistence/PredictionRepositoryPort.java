package com.predictorama.backend.domain.port.persistence;

import com.predictorama.backend.domain.entity.Prediction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PredictionRepositoryPort {

    Prediction save(Prediction prediction);

    Optional<Prediction> findById(UUID id);

    Optional<Prediction> findByUserIdAndMatchIdAndGroupId(UUID userId, UUID matchId, UUID groupId);

    List<Prediction> findByMatchIdAndGroupId(UUID matchId, UUID groupId);

    List<Prediction> findByUserIdAndGroupId(UUID userId, UUID groupId);

    List<Prediction> findByUserId(UUID userId);

    List<Prediction> findByGroupId(UUID groupId);
}
