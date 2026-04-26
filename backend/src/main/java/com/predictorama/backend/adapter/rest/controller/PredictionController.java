package com.predictorama.backend.adapter.rest.controller;

import static com.predictorama.backend.config.ApiPaths.PREDICTIONS;
import static com.predictorama.backend.config.ApiPaths.V1;

import com.predictorama.backend.adapter.rest.dto.CreatePredictionRequest;
import com.predictorama.backend.adapter.rest.dto.PredictionResponse;
import com.predictorama.backend.adapter.rest.dto.TournamentPredictionsResponse;
import com.predictorama.backend.adapter.rest.mapper.TournamentPredictionsRestMapper;
import com.predictorama.backend.config.AuthUtils;
import com.predictorama.backend.domain.entity.Prediction;
import com.predictorama.backend.domain.entity.Score;
import com.predictorama.backend.domain.service.PredictionService;
import com.predictorama.backend.domain.service.TournamentPredictionQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(V1 + PREDICTIONS)
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionService predictionService;
    private final TournamentPredictionQueryService tournamentPredictionQueryService;

    @PostMapping
    public PredictionResponse createPrediction(
            @Valid @RequestBody CreatePredictionRequest request
    ) {
        var userId = AuthUtils.currentUserId();

        Prediction savedPrediction = predictionService.savePrediction(
                userId,
                request.getGroupId(),
                request.getMatchId(),
                request.getHomeScore(),
                request.getAwayScore(),
                request.getPredictedWinner()
        );

        return toResponse(savedPrediction);
    }

    @GetMapping
    public TournamentPredictionsResponse getTournamentPredictions(
            @RequestParam String competition,
            @RequestParam UUID groupId
    ) {
        var userId = AuthUtils.currentUserId();

        return TournamentPredictionsRestMapper.toResponse(
                tournamentPredictionQueryService.getTournamentPredictions(
                        competition,
                        userId,
                        groupId
                )
        );
    }

    private PredictionResponse toResponse(Prediction prediction) {
        Score score = prediction.requirePrimaryPredictedScore();

        return new PredictionResponse(
                prediction.getId(),
                prediction.getMatchId(),
                score.getHomeScore(),
                score.getAwayScore(),
                prediction.getPredictedWinner(),
                prediction.getSubmittedAt()
        );
    }
}