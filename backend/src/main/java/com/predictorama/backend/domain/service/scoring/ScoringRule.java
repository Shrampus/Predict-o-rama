package com.predictorama.backend.domain.service.scoring;

import com.predictorama.backend.domain.entity.Prediction;
import com.predictorama.backend.domain.entity.Score;
import com.predictorama.backend.domain.entity.Winner;

public interface ScoringRule {
    int evaluate(Prediction prediction, Score actualScore, Winner actualWinner);
    String name();
}
