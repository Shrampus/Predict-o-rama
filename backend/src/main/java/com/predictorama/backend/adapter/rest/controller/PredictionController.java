package com.predictorama.backend.adapter.rest.controller;

import com.predictorama.backend.adapter.rest.dto.CreatePredictionRequest;
import com.predictorama.backend.adapter.rest.dto.PredictionResponse;
import com.predictorama.backend.domain.entity.Prediction;
import com.predictorama.backend.domain.entity.Score;
import com.predictorama.backend.domain.service.PredictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.predictorama.backend.adapter.rest.dto.PredictionPageResponseDto;
import com.predictorama.backend.adapter.rest.mapper.PredictionPageMapper;
import com.predictorama.backend.domain.entity.Prediction;
import com.predictorama.backend.domain.port.persistence.PredictionRepositoryPort;
import com.predictorama.backend.domain.service.PredictionPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/predictions")
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionService predictionService;

    @Autowired
    public PredictionController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @PostMapping
    public PredictionResponse createPrediction(@RequestBody CreatePredictionRequest request) {

        Prediction prediction = Prediction.builder()
                .userId(request.userId)
                .matchId(request.matchId)
                .groupId(request.groupId)
                .predictedScores(List.of(new Score(request.homeScore, request.awayScore)))
                .predictedWinner(null)
                .build();

        Prediction savedPrediction = predictionService.createPrediction(prediction);

        PredictionResponse response = new PredictionResponse();
        response.userName = "To do Ada";
        response.groupName = "To do Ada";
        response.matchResult = "To do Ada";
        response.predictedScoreHome = savedPrediction.getPredictedScores().getFirst().getScore();
        response.predictedScoreAway = savedPrediction.getPredictedScores().getFirst().getScore();
        response.isWinner = savedPrediction.getPredictedWinner() != null;

        return response;
    }



    private final PredictionPageService predictionPageService;
    private final PredictionRepositoryPort predictionRepositoryPort;

    @GetMapping
    public PredictionPageResponseDto getPredictions(
            @RequestParam String competition,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) UUID groupId
    ) {
        var matches = predictionPageService.getPredictionPageMatches(competition);

        Map<UUID, Prediction> predictionsByMatchId = loadPredictionsByMatchId(userId, groupId);

        return PredictionPageResponseDto.builder()
                .matches(matches.stream()
                        .map(match -> PredictionPageMapper.toDto(match, predictionsByMatchId.get(match.getId())))
                        .toList())
                .build();
    }

    private Map<UUID, Prediction> loadPredictionsByMatchId(UUID userId, UUID groupId) {
        if (userId == null || groupId == null) {
            return Map.of();
        }

        List<Prediction> predictions = predictionRepositoryPort.findByUserId(userId).stream()
                .filter(prediction -> groupId.equals(prediction.getGroupId()))
                .toList();

        return predictions.stream()
                .collect(Collectors.toMap(
                        Prediction::getMatchId,
                        Function.identity(),
                        (first, second) -> first
                ));
    }
}