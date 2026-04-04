package com.predictorama.backend.adapter.rest.controller;

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