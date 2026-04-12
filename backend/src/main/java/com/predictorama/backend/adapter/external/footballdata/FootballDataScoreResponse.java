package com.predictorama.backend.adapter.external.footballdata;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FootballDataScoreResponse {
    private String winner;
    private FootballDataScoreDetailedResponse fullTime;
}
