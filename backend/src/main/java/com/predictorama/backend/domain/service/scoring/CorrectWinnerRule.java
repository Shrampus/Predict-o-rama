package com.predictorama.backend.domain.service.scoring;

import com.predictorama.backend.domain.entity.Prediction;
import com.predictorama.backend.domain.entity.Score;
import com.predictorama.backend.domain.entity.Winner;

public class CorrectWinnerRule implements ScoringRule {

    @Override
    public int evaluate(Prediction prediction, Score actualScore, Winner actualWinner) {
        Winner predictedWinner = prediction.getPredictedWinner();
        if (predictedWinner == null) {
            return 0;
        }
        if (predictedWinner.equals(actualWinner)) {
            return 1;
        }
        return 0;
    }

    @Override
    public String name() {
        return "CORRECT_WINNER";
    }
}
