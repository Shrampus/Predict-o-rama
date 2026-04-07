package com.predictorama.backend.domain.query;

import com.predictorama.backend.domain.entity.Winner;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class TournamentMatchPredictionView {

    private UUID matchId;
    private String externalMatchId;

    private String homeTeamName;
    private String awayTeamName;

    private String homeTeamImage;
    private String awayTeamImage;

    private Instant kickoffTime;
    private String matchStatus;

    private UUID predictionId;
    private Integer predictedHomeScore;
    private Integer predictedAwayScore;
    private Winner predictedWinner;
}