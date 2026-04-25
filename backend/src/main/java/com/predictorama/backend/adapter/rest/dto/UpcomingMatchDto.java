package com.predictorama.backend.adapter.rest.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class UpcomingMatchDto {
    private UUID matchId;
    private String homeTeamName;
    private String homeTeamImage;
    private String awayTeamName;
    private String awayTeamImage;
    private Instant kickoffTime;
    private String tournamentName;
    private List<GroupReferenceDto> groups;
}
