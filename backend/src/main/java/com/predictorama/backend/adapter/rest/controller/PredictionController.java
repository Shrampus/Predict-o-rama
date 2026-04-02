package com.predictorama.backend.adapter.rest.controller;

import com.predictorama.backend.adapter.rest.dto.PredictionPageResponseDto;
import com.predictorama.backend.domain.service.PredictionPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/predictions")
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionPageService predictionPageService;

    @GetMapping
    public PredictionPageResponseDto getPredictions(@RequestParam String competition) {
        return predictionPageService.getPredictionPage(competition);
    }
}
