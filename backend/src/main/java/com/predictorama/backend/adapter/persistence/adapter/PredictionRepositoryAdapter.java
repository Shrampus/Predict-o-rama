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
        List<PredictionScoreEntity> scoreEntities = prediction.getPredictedScores().stream()
                .map(score -> PredictionScoreMapper.toEntity(saved.getId(), score))
                .toList();
        predictionScoreRepository.saveAll(scoreEntities);
        log.debug("Prediction saved - id={}, scores={}", saved.getId(), scoreEntities.size());
        List<Score> scores = scoreEntities.stream().map(PredictionScoreMapper::toDomain).toList();
        return PredictionMapper.toDomain(saved, scores);
    }

    @Override
    public Optional<Prediction> findById(UUID id) {
        return jpaRepository.findById(id).map(entity -> {
            List<Score> scores = loadScores(entity.getId());
            return PredictionMapper.toDomain(entity, scores);
        });
    }

    @Override
    public Optional<Prediction> findByUserIdAndMatchIdAndGroupId(UUID userId, UUID matchId, UUID groupId) {
        return jpaRepository.findByUserIdAndMatchIdAndGroupId(userId, matchId, groupId).map(entity -> {
            List<Score> scores = loadScores(entity.getId());
            return PredictionMapper.toDomain(entity, scores);
        });
    }

    @Override
    public List<Prediction> findByMatchIdAndGroupId(UUID matchId, UUID groupId) {
        return jpaRepository.findByMatchIdAndGroupId(matchId, groupId).stream()
                .map(entity -> PredictionMapper.toDomain(entity, loadScores(entity.getId())))
                .toList();
    }

    @Override
    public List<Prediction> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).stream()
                .map(entity -> PredictionMapper.toDomain(entity, loadScores(entity.getId())))
                .toList();
    }

    @Override
    public List<Prediction> findByGroupId(UUID groupId) {
        return jpaRepository.findByGroupId(groupId).stream()
                .map(entity -> PredictionMapper.toDomain(entity, loadScores(entity.getId())))
                .toList();
    }

    private List<Score> loadScores(UUID predictionId) {
        return predictionScoreRepository.findByPredictionId(predictionId).stream()
                .map(PredictionScoreMapper::toDomain)
                .toList();
    }
}
