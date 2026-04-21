package com.predictorama.backend.adapter.rest.mapper;

import com.predictorama.backend.adapter.rest.dto.GroupReferenceDto;
import com.predictorama.backend.adapter.rest.dto.UpcomingMatchDto;
import com.predictorama.backend.domain.service.result.UpcomingMatchResult;

public final class UpcomingMatchRestMapper {

    private UpcomingMatchRestMapper() {
    }

    public static UpcomingMatchDto toDto(UpcomingMatchResult result) {
        var match = result.getMatch();
        var groups = result.getUserGroups().stream()
                .map(group -> GroupReferenceDto.builder()
                        .groupId(group.getId().toString())
                        .groupName(group.getName())
                        .competitionId(result.getCompetitionCode())
                        .build())
                .toList();

        return UpcomingMatchDto.builder()
                .matchId(match.getId())
                .homeTeamName(match.getHomeTeam().getName())
                .homeTeamImage(match.getHomeTeam().getImageUrl())
                .awayTeamName(match.getAwayTeam().getName())
                .awayTeamImage(match.getAwayTeam().getImageUrl())
                .kickoffTime(match.getKickoffTime())
                .groups(groups)
                .build();
    }
}
