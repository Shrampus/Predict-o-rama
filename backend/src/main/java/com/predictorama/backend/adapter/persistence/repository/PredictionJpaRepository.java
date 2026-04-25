package com.predictorama.backend.adapter.persistence.repository;

import com.predictorama.backend.adapter.persistence.entity.PredictionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PredictionJpaRepository extends JpaRepository<PredictionEntity, UUID> {

    Optional<PredictionEntity> findByUserIdAndMatchIdAndGroupId(UUID userId, UUID matchId, UUID groupId);

    List<PredictionEntity> findByMatchIdAndGroupId(UUID matchId, UUID groupId);

    List<PredictionEntity> findByUserIdAndGroupId(UUID userId, UUID groupId);

    List<PredictionEntity> findByUserId(UUID userId);

    List<PredictionEntity> findByGroupId(UUID groupId);

    List<PredictionEntity> findByMatchId(UUID matchId);

    List<PredictionEntity> findByGroupIdAndMatchIdIn(UUID groupId, List<UUID> matchIds);

    @Modifying
    @Query("UPDATE PredictionEntity p SET p.result = :result WHERE p.id = :id")
    void updateResult(@Param("id") UUID id, @Param("result") int result);
}
