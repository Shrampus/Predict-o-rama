package com.predictorama.backend.adapter.rest.controller;

import static com.predictorama.backend.config.ApiPaths.MATCHES;
import static com.predictorama.backend.config.ApiPaths.V1;

import com.predictorama.backend.adapter.rest.dto.UpcomingMatchDto;
import com.predictorama.backend.adapter.rest.mapper.UpcomingMatchRestMapper;
import com.predictorama.backend.config.AuthUtils;
import com.predictorama.backend.domain.service.UpcomingMatchQueryService;
import com.predictorama.backend.domain.service.UserUpcomingMatchQueryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(V1 + MATCHES)
@RequiredArgsConstructor
public class MatchController {

    private static final Logger log = LoggerFactory.getLogger(MatchController.class);

    private final UpcomingMatchQueryService upcomingMatchQueryService;
    private final UserUpcomingMatchQueryService userUpcomingMatchQueryService;

    @GetMapping("/upcoming")
    public List<UpcomingMatchDto> getUpcomingMatches(HttpServletRequest request) {
        log.info("GET {}", request.getRequestURI());
        return upcomingMatchQueryService.getGenericUpcomingMatches().stream()
                .map(UpcomingMatchRestMapper::toDto)
                .toList();
    }

    @GetMapping("/upcoming/my")
    public List<UpcomingMatchDto> getMyUpcomingMatches(HttpServletRequest request) {
        var userId = AuthUtils.currentUserId();
        log.info("GET {} - userId={}", request.getRequestURI(), userId);
        return userUpcomingMatchQueryService.getUpcomingMatches(userId).stream()
                .map(UpcomingMatchRestMapper::toDto)
                .toList();
    }
}
