package com.predictorama.backend.adapter.rest.controller;

import com.predictorama.backend.adapter.rest.dto.CreatePredictionRequest;
import com.predictorama.backend.adapter.rest.dto.PredictionResponse;
import com.predictorama.backend.domain.entity.Prediction;
import com.predictorama.backend.domain.entity.Score;
import com.predictorama.backend.domain.service.PredictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/predictions")
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
        response.userName = "Placeholder";
        response.groupName = "Placeholder";
        response.matchResult = "Placeholder";
        response.predictedScore = savedPrediction.getPredictedScores().getFirst().getScore();
        response.isWinner = savedPrediction.getPredictedWinner() != null;

        return response;
    }

    @GetMapping("/user/{userId}")
    public List<PredictionResponse> getPredictionsByUser(@PathVariable UUID userId) {

        List<Prediction> predictions = predictionService.getPredictionsByUser(userId);

        return predictions.stream().map(pred -> {
            PredictionResponse response = new PredictionResponse();
            response.userName = "Placeholder";
            response.groupName = "Placeholder";
            response.matchResult = "Placeholder";
            response.predictedScore = pred.getPredictedScores().getFirst().getScore();
            response.isWinner = pred.getPredictedWinner() != null;
            return response;
        }).toList();
    }
}