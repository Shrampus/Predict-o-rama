package com.predictorama.backend.domain.service;

import com.predictorama.backend.domain.entity.Prediction;
import com.predictorama.backend.domain.port.persistence.PredictionRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service

public class PredictionService {

    private final PredictionRepositoryPort predictionRepository;

    public PredictionService(PredictionRepositoryPort predictionRepository) {
        this.predictionRepository = predictionRepository;
    }


    public Prediction createPrediction(Prediction prediction) {

        return predictionRepository.save(prediction);
    }


    public List<Prediction> getPredictionsByUser(UUID userId) {
        return predictionRepository.findByUserId(userId);
    }
}


