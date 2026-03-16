package com.predictorama.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/predictions")
public class PredictionController {

    @GetMapping
    public Map<String, Object> getPredictions() {
        return Map.of(
                "message", "Predictions endpoint placeholder",
                "data", List.of()
        );
    }
}