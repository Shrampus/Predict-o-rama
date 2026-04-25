package com.predictorama.backend.domain.service;

import com.predictorama.backend.adapter.rest.dto.RulesetConfigRequestDto;
import com.predictorama.backend.adapter.rest.dto.RulesetResponseDto;
import com.predictorama.backend.domain.entity.Ruleset;
import com.predictorama.backend.domain.port.persistence.RulesetRepositoryPort;
import com.predictorama.backend.domain.service.scoring.ScoringRule;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.predictorama.backend.adapter.rest.mapper.RulesetMapper.toResponse;

@Service
@RequiredArgsConstructor
public class RulesetService {
    private final RulesetRepositoryPort rulesetRepositoryPort;
    private final AccessService accessService;
    private final PredictionScoringService predictionScoringService;

    private final List<ScoringRule> allScoringRules;

    public static final Map<String, Integer> DEFAULT_RULE_POINTS = Map.of(
            "CORRECT_WINNER", 1,
            "CORRECT_GOAL_DIFFERENCE", 2,
            "EXACT_SCORE", 3
    );

    public void setDefaultResultsetForGroupTournament(UUID groupId, UUID tournamentId) {
        rulesetRepositoryPort.upsertForGroupTournament(groupId, tournamentId, DEFAULT_RULE_POINTS);
    }

    public RulesetResponseDto getRuleset(UUID userId, UUID groupId, UUID tournamentId) {
        accessService.requireActiveMembership(userId, groupId);
        var ruleset = rulesetRepositoryPort
                .findByGroupIdAndTournamentId(groupId, tournamentId)
                .orElseGet(() -> Ruleset.builder().id(UUID.randomUUID()).rulePoints(DEFAULT_RULE_POINTS).build());
        return toResponse(ruleset, getDisabledRules(ruleset.getRulePoints().keySet()));
    }

    private Map<String, Integer> getDisabledRules(Set<String> activeRuleNames) {
        return allScoringRules.stream()
                .map(ScoringRule::name)
                .filter(name -> !activeRuleNames.contains(name))
                .collect(Collectors.toMap(name -> name, name -> DEFAULT_RULE_POINTS.getOrDefault(name, 1)));
    }

    public RulesetResponseDto updateRuleset(UUID adminUserId, UUID groupId, UUID tournamentId,
                                         RulesetConfigRequestDto request) {
        accessService.requireAdminMembership(adminUserId, groupId);

        Ruleset saved = rulesetRepositoryPort.upsertForGroupTournament(groupId, tournamentId, request.getRulePoints());
        predictionScoringService.recalculatePredictionScores(groupId, tournamentId, saved); // @Async — returns immediately
        return toResponse(saved, getDisabledRules(saved.getRulePoints().keySet()));
    }
}
