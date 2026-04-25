package com.predictorama.backend.domain.service.scoring;

import com.predictorama.backend.domain.entity.Prediction;
import com.predictorama.backend.domain.entity.Score;
import com.predictorama.backend.domain.entity.Winner;

public class CorrectGoalDifferenceRule implements ScoringRule {

    @Override
    public boolean matches(Prediction prediction, Score actualScore, Winner actualWinner) {
        Score predictedScore = prediction.primaryPredictedScore().orElse(null);
        if (predictedScore == null) {
            return false;
        }
        if ((predictedScore.getHomeScore() - predictedScore.getAwayScore()) ==
            (actualScore.getHomeScore() - actualScore.getAwayScore())) {
            return true;
        }
        return false;
    }

    @Override
    public String name() {
        return "CORRECT_GOAL_DIFFERENCE";
    }
}
