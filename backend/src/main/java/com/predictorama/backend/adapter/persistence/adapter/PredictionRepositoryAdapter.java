package com.predictorama.backend.adapter.persistence.adapter;

import com.predictorama.backend.adapter.persistence.entity.PredictionEntity;
import com.predictorama.backend.adapter.persistence.entity.PredictionScoreEntity;
import com.predictorama.backend.adapter.persistence.mapper.PredictionMapper;
import com.predictorama.backend.adapter.persistence.mapper.PredictionScoreMapper;
import com.predictorama.backend.adapter.persistence.repository.PredictionJpaRepository;
import com.predictorama.backend.adapter.persistence.repository.PredictionScoreJpaRepository;
import com.predictorama.backend.domain.entity.Prediction;
import com.predictorama.backend.domain.entity.Score;
import com.predictorama.backend.domain.port.persistence.PredictionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PredictionRepositoryAdapter implements PredictionRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(PredictionRepositoryAdapter.class);

    private final PredictionJpaRepository jpaRepository;
    private final PredictionScoreJpaRepository predictionScoreRepository;

    @Override
    @Transactional
    public Prediction save(Prediction prediction) {
        log.debug("Saving prediction - id={}, userId={}, matchId={}", prediction.getId(), prediction.getUserId(), prediction.getMatchId());
        
        PredictionEntity saved = jpaRepository.save(PredictionMapper.toEntity(prediction));
        predictionScoreRepository.deleteByPredictionId(saved.getId());

        List<PredictionScoreEntity> scoreEntities = PredictionScoreMapper.toEntities(
                saved.getId(),
                prediction.getPredictedScores()
        );
        if (!scoreEntities.isEmpty()) {
            predictionScoreRepository.saveAll(scoreEntities);
        }

        log.debug("Prediction saved - id={}, scores={}", saved.getId(), scoreEntities.size());

        return toDomainWithScores(saved);
    }

    @Override
    public Optional<Prediction> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomainWithScores);
    }

    @Override
    public Optional<Prediction> findByUserIdAndMatchIdAndGroupId(UUID userId, UUID matchId, UUID groupId) {
        return jpaRepository.findByUserIdAndMatchIdAndGroupId(userId, matchId, groupId)
                .map(this::toDomainWithScores);
    }

    @Override
    public List<Prediction> findByMatchIdAndGroupId(UUID matchId, UUID groupId) {
        return jpaRepository.findByMatchIdAndGroupId(matchId, groupId).stream()
                .map(this::toDomainWithScores)
                .toList();
    }

    @Override
    public List<Prediction> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).stream()
                .map(this::toDomainWithScores)
                .toList();
    }

    @Override
    public List<Prediction> findByUserIdAndGroupId(UUID userId, UUID groupId) {
        return jpaRepository.findByUserIdAndGroupId(userId, groupId).stream()
                .map(this::toDomainWithScores)
                .toList();
    }

    @Override
    public List<Prediction> findByGroupId(UUID groupId) {
        return jpaRepository.findByGroupId(groupId).stream()
                .map(this::toDomainWithScores)
                .toList();
    }

    private Prediction toDomainWithScores(PredictionEntity entity) {
        return PredictionMapper.toDomain(entity, loadScores(entity.getId()));
    }

    private List<Score> loadScores(UUID predictionId) {
        return predictionScoreRepository.findByPredictionId(predictionId).stream()
                .map(PredictionScoreMapper::toDomain)
                .toList();
    }
}