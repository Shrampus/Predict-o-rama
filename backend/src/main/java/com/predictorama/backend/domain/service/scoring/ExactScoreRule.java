package com.predictorama.backend.domain.service.scoring;
import com.predictorama.backend.domain.entity.Prediction;
import com.predictorama.backend.domain.entity.Score;
import com.predictorama.backend.domain.entity.Winner;


public class ExactScoreRule implements ScoringRule {
    @Override
    public int evaluate(Prediction prediction, Score actualScore, Winner actualWinner){
            Score predictedScore = prediction.primaryPredictedScore().orElse(null);
            if(predictedScore == null) {
                return 0;
            }
            else if(predictedScore.getHomeScore().equals(actualScore.getHomeScore()) && predictedScore.getAwayScore().equals(actualScore.getAwayScore()))
            {
                return 3;
            }
            else
            {
                return 0;
            }

    }
    @Override
    public String name() {
        return "Exact Score";
    }
}
