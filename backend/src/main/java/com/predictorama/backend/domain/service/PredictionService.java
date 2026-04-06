package com.predictorama.backend.domain.service;

import com.predictorama.backend.domain.entity.Prediction;
import com.predictorama.backend.domain.entity.Score;
import com.predictorama.backend.domain.entity.Winner;
import com.predictorama.backend.domain.exception.InvalidPredictionException;
import com.predictorama.backend.domain.port.persistence.PredictionRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PredictionService {

    private final PredictionRepositoryPort predictionRepository;

    public PredictionService(PredictionRepositoryPort predictionRepository) {
        this.predictionRepository = predictionRepository;
    }

    public Prediction savePrediction(
            UUID userId,
            UUID groupId,
            UUID matchId,
            int homeScore,
            int awayScore,
            Winner predictedWinner
    ) {
        validatePredictionInput(homeScore, awayScore, predictedWinner);

        Optional<Prediction> existingPrediction =
                predictionRepository.findByUserIdAndMatchIdAndGroupId(userId, matchId, groupId);

        Prediction prediction = existingPrediction
                .map(existing -> buildUpdatedPrediction(
                        existing,
                        homeScore,
                        awayScore,
                        predictedWinner
                ))
                .orElseGet(() -> buildNewPrediction(
                        userId,
                        groupId,
                        matchId,
                        homeScore,
                        awayScore,
                        predictedWinner
                ));

        return predictionRepository.save(prediction);
    }

    public Map<UUID, Prediction> getPredictionsByUserAndGroup(UUID userId, UUID groupId) {
        return predictionRepository.findByUserIdAndGroupId(userId, groupId).stream()
                .collect(Collectors.toMap(
                        Prediction::getMatchId,
                        Function.identity(),
                        (first, second) -> {
                            throw new IllegalStateException(
                                    "Duplicate predictions detected for userId=%s groupId=%s matchId=%s"
                                            .formatted(userId, groupId, first.getMatchId())
                            );
                        }
                ));
    }

    public List<Prediction> getPredictionsByUser(UUID userId) {
        return predictionRepository.findByUserId(userId);
    }

    private Prediction buildUpdatedPrediction(
            Prediction existing,
            int homeScore,
            int awayScore,
            Winner predictedWinner
    ) {
        return Prediction.builder()
                .id(existing.getId())
                .userId(existing.getUserId())
                .matchId(existing.getMatchId())
                .groupId(existing.getGroupId())
                .predictedScores(List.of(normalTimeScore(homeScore, awayScore)))
                .predictedWinner(predictedWinner)
                .submittedAt(Instant.now())
                .result(existing.getResult())
                .build();
    }

    private Prediction buildNewPrediction(
            UUID userId,
            UUID groupId,
            UUID matchId,
            int homeScore,
            int awayScore,
            Winner predictedWinner
    ) {
        return Prediction.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .matchId(matchId)
                .groupId(groupId)
                .predictedScores(List.of(normalTimeScore(homeScore, awayScore)))
                .predictedWinner(predictedWinner)
                .submittedAt(Instant.now())
                .result(null)
                .build();
    }

    private Score normalTimeScore(int homeScore, int awayScore) {
        return Score.builder()
                .homeScore(homeScore)
                .awayScore(awayScore)
                .scoreType(Score.ScoreType.NORMAL_TIME)
                .build();
    }

    private void validatePredictionInput(int homeScore, int awayScore, Winner predictedWinner) {
        if (homeScore < 0 || awayScore < 0) {
            throw new InvalidPredictionException("Scores must be non-negative.");
        }

        if (predictedWinner == null) {
            throw new InvalidPredictionException("Predicted winner is required.");
        }
    }
}