package com.predictorama.backend.adapter.external.footballdata;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FootballDataMatchResponse {
    private Integer id;
    private String utcDate;
    private String status;
    private FootballDataTeamResponse homeTeam;
    private FootballDataTeamResponse awayTeam;
    private FootballDataScoreResponse score;
}