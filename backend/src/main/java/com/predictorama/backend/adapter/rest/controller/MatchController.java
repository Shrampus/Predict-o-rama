package com.predictorama.backend.adapter.rest.controller;

import com.predictorama.backend.adapter.rest.SessionService;
import com.predictorama.backend.adapter.rest.dto.GroupReferenceDto;
import com.predictorama.backend.adapter.rest.dto.UpcomingMatchDto;
import com.predictorama.backend.domain.service.UpcomingMatchQueryService;
import com.predictorama.backend.domain.service.result.UpcomingMatchResult;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private static final Logger log = LoggerFactory.getLogger(MatchController.class);

    private final UpcomingMatchQueryService upcomingMatchQueryService;
    private final SessionService sessionService;

    @GetMapping("/upcoming")
    public List<UpcomingMatchDto> getUpcomingMatches() {
        log.info("GET /api/matches/upcoming");
        return upcomingMatchQueryService.getGenericUpcomingMatches().stream()
                .map(this::toDto)
                .toList();
    }

    @GetMapping("/upcoming/my")
    public List<UpcomingMatchDto> getMyUpcomingMatches(HttpSession session) {
        var userId = sessionService.getUserIdOrThrow(session);
        log.info("GET /api/matches/upcoming/my - userId={}", userId);
        return upcomingMatchQueryService.getUpcomingMatches(userId).stream()
                .map(this::toDto)
                .toList();
    }

    private UpcomingMatchDto toDto(UpcomingMatchResult result) {
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
