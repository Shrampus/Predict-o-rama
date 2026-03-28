package com.predictorama.backend.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class Match {
    private UUID id;
    private UUID tournamentId;
    private String name;
    private String description;
    private Team homeTeam;
    private Team awayTeam;
    private MatchStatus matchStatus;
    private Instant kickoffTime;
    private List<Score> scores;
    private Winner winner;
    private String externalId;

    public enum MatchStatus{
        SCHEDULED, LIVE, COMPLETED, CANCELLED
    }
}
