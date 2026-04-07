package com.predictorama.backend.adapter.rest.controller;

import com.predictorama.backend.adapter.rest.SessionService;
import com.predictorama.backend.adapter.rest.dto.CreatePredictionRequest;
import com.predictorama.backend.adapter.rest.dto.PredictionResponse;
import com.predictorama.backend.adapter.rest.dto.TournamentPredictionsResponse;
import com.predictorama.backend.adapter.rest.mapper.TournamentPredictionsRestMapper;
import com.predictorama.backend.domain.entity.Prediction;
import com.predictorama.backend.domain.entity.Score;
import com.predictorama.backend.domain.service.PredictionService;
import com.predictorama.backend.domain.service.TournamentPredictionQueryService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/predictions")
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionService predictionService;
    private final TournamentPredictionQueryService tournamentPredictionQueryService;
    private final SessionService sessionService;

    @PostMapping
    public PredictionResponse createPrediction(
            @Valid @RequestBody CreatePredictionRequest request,
            HttpSession session
    ) {
        UUID userId = sessionService.getUserIdOrThrow(session);

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
            @RequestParam UUID groupId,
            HttpSession session
    ) {
        UUID userId = sessionService.getUserIdOrThrow(session);

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