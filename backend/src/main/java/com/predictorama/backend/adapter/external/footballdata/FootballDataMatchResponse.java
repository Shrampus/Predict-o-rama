package com.predictorama.backend.adapter.external.footballdata;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FootballDataMatchResponse {
    private FootballDataSeasonResponse season;
    private Integer id;
    private String utcDate;
    private String status;
    private Integer matchday;
    private String stage;
    private String group;
    private FootballDataTeamResponse homeTeam;
    private FootballDataTeamResponse awayTeam;
    private FootballDataScoreResponse score;
}
