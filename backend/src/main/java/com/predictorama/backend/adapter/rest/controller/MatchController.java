package com.predictorama.backend.adapter.rest.controller;

import com.predictorama.backend.adapter.rest.dto.UpcomingMatchDto;
import com.predictorama.backend.domain.service.UpcomingMatchQueryService;
import com.predictorama.backend.domain.service.result.UpcomingMatchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final UpcomingMatchQueryService upcomingMatchQueryService;

    @GetMapping("/upcoming")
    public List<UpcomingMatchDto> getUpcomingMatches() {
        return upcomingMatchQueryService.getGenericUpcomingMatches().stream()
                .map(this::toDto)
                .toList();
    }

    private UpcomingMatchDto toDto(UpcomingMatchResult result) {
        var match = result.getMatch();
        var groups = result.getUserGroups().stream()
                .map(group -> com.predictorama.backend.adapter.rest.dto.GroupReferenceDto.builder()
                        .groupId(group.getId().toString())
                        .groupName(group.getName())
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
