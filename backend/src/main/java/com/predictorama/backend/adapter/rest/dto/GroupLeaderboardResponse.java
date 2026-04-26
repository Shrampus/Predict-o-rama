package com.predictorama.backend.adapter.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class GroupLeaderboardResponse {
    private UUID tournamentId;
    private String competitionCode;
    private String tournamentName;
    private List<GroupLeaderboardEntryResponse> entries;
}
