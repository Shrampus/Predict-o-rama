package com.predictorama.backend.adapter.external.footballdata;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class FootballDataMatchesResponse {
    private List<FootballDataMatchResponse> matches;
}