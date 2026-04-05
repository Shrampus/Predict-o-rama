package com.predictorama.backend.adapter.rest.controller;

import com.predictorama.backend.adapter.rest.dto.CreatePredictionRequest;
import com.predictorama.backend.adapter.rest.dto.PredictionResponse;
import com.predictorama.backend.domain.entity.Prediction;
import com.predictorama.backend.domain.entity.Score;
import com.predictorama.backend.domain.port.persistence.PredictionRepositoryPort;
import com.predictorama.backend.domain.service.PredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/predictions")
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionService predictionService;

    private final PredictionRepositoryPort predictionRepositoryPort;



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



}