package com.predictorama.backend.adapter.rest.controller;

import com.predictorama.backend.adapter.rest.dto.RulesetConfigRequestDto;
import com.predictorama.backend.adapter.rest.dto.RulesetResponseDto;
import com.predictorama.backend.adapter.rest.mapper.RulesetMapper;
import com.predictorama.backend.domain.service.RulesetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/groups/{groupId}/tournaments/{tournamentId}/ruleset")
@RequiredArgsConstructor
public class RulesetController {

    private static final Logger log = LoggerFactory.getLogger(RulesetController.class);

    private final RulesetService rulesetService;

    private UUID currentUserId() {
        return UUID.fromString((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @GetMapping
    public ResponseEntity<RulesetResponseDto> getRuleset(
            @PathVariable UUID groupId,
            @PathVariable UUID tournamentId
    ) {
        UUID userId = currentUserId();
        log.info("GET /api/groups/{}/tournaments/{}/ruleset - userId={}", groupId, tournamentId, userId);
        RulesetService.RulesetResult result = rulesetService.getRuleset(userId, groupId, tournamentId);
        return ResponseEntity.ok(RulesetMapper.toResponse(result.ruleset(), result.disabledRules()));
    }

    @PutMapping
    public ResponseEntity<RulesetResponseDto> updateRuleset(
            @PathVariable UUID groupId,
            @PathVariable UUID tournamentId,
            @Valid @RequestBody RulesetConfigRequestDto request
    ) {
        UUID userId = currentUserId();
        log.info("PUT /api/groups/{}/tournaments/{}/ruleset - userId={}", groupId, tournamentId, userId);
        RulesetService.RulesetResult result = rulesetService.updateRuleset(userId, groupId, tournamentId, request.getRulePoints());
        return ResponseEntity.ok(RulesetMapper.toResponse(result.ruleset(), result.disabledRules()));
    }
}
