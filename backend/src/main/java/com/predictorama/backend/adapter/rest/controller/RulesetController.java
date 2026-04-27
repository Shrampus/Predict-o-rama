package com.predictorama.backend.adapter.rest.controller;

import com.predictorama.backend.adapter.rest.dto.RulesetConfigRequestDto;
import com.predictorama.backend.adapter.rest.dto.RulesetResponseDto;
import com.predictorama.backend.adapter.rest.mapper.RulesetMapper;
import com.predictorama.backend.config.AuthUtils;
import com.predictorama.backend.domain.service.RulesetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.predictorama.backend.config.ApiPaths.*;

@RestController
@RequestMapping(V1 + GROUPS + "/{groupId}/tournaments/{tournamentId}/ruleset")
@RequiredArgsConstructor
public class RulesetController {

    private static final Logger log = LoggerFactory.getLogger(RulesetController.class);

    private final RulesetService rulesetService;

    @GetMapping
    public ResponseEntity<RulesetResponseDto> getRuleset(
            @PathVariable UUID groupId,
            @PathVariable UUID tournamentId,
            HttpServletRequest httpRequest
    ) {
        UUID userId = AuthUtils.currentUserId();
        log.info("GET {} - userId={}", httpRequest.getRequestURI(), userId);
        RulesetService.RulesetResult result = rulesetService.getRuleset(userId, groupId, tournamentId);
        return ResponseEntity.ok(RulesetMapper.toResponse(result.ruleset(), result.disabledRules()));
    }

    @PutMapping
    public ResponseEntity<RulesetResponseDto> updateRuleset(
            @PathVariable UUID groupId,
            @PathVariable UUID tournamentId,
            @Valid @RequestBody RulesetConfigRequestDto request,
            HttpServletRequest httpRequest
    ) {
        UUID userId = AuthUtils.currentUserId();
        log.info("PUT {} - userId={}", httpRequest.getRequestURI(), userId);
        RulesetService.RulesetResult result = rulesetService.updateRuleset(userId, groupId, tournamentId, request.getRulePoints());
        return ResponseEntity.ok(RulesetMapper.toResponse(result.ruleset(), result.disabledRules()));
    }
}
