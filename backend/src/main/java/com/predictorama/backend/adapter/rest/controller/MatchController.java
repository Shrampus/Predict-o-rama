package com.predictorama.backend.adapter.rest.controller;

import com.predictorama.backend.adapter.rest.SessionService;
import com.predictorama.backend.adapter.rest.dto.UpcomingMatchDto;
import com.predictorama.backend.adapter.rest.mapper.UpcomingMatchRestMapper;
import com.predictorama.backend.domain.service.UpcomingMatchQueryService;
import com.predictorama.backend.domain.service.UserUpcomingMatchQueryService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private static final Logger log = LoggerFactory.getLogger(MatchController.class);

    private UUID currentUserId() {
        return UUID.fromString((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    private final UpcomingMatchQueryService upcomingMatchQueryService;
    private final UserUpcomingMatchQueryService userUpcomingMatchQueryService;
    private final SessionService sessionService;

    @GetMapping("/upcoming")
    public List<UpcomingMatchDto> getUpcomingMatches() {
        log.info("GET /api/matches/upcoming");
        return upcomingMatchQueryService.getGenericUpcomingMatches().stream()
                .map(UpcomingMatchRestMapper::toDto)
                .toList();
    }

    @GetMapping("/upcoming/my")
    public List<UpcomingMatchDto> getMyUpcomingMatches(HttpSession session) {
        var userId = currentUserId();
        log.info("GET /api/matches/upcoming/my - userId={}", userId);
        return userUpcomingMatchQueryService.getUpcomingMatches(userId).stream()
                .map(UpcomingMatchRestMapper::toDto)
                .toList();
    }
}
