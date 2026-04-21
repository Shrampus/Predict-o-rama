package com.predictorama.backend.adapter.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class GroupLeaderboardEntryResponse {
    private UUID userId;
    private String name;
    private int totalScore;
    private int scoredPredictions;
    private int totalPredictions;
}
