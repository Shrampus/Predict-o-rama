package com.predictorama.backend.domain.service;

import com.predictorama.backend.domain.entity.Ruleset;
import com.predictorama.backend.domain.port.persistence.RulesetRepositoryPort;
import com.predictorama.backend.domain.service.scoring.ScoringRule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public RulesetResult getRuleset(UUID userId, UUID groupId, UUID tournamentId) {
        accessService.requireActiveMembership(userId, groupId);
        Ruleset ruleset = rulesetRepositoryPort
                .findByGroupIdAndTournamentId(groupId, tournamentId)
                .orElseGet(() -> Ruleset.builder().id(UUID.randomUUID()).rulePoints(DEFAULT_RULE_POINTS).build());
        return new RulesetResult(ruleset, getDisabledRules(ruleset.getRulePoints().keySet()));
    }

    public RulesetResult updateRuleset(UUID adminUserId, UUID groupId, UUID tournamentId,
                                       Map<String, Integer> rulePoints) {
        accessService.requireAdminMembership(adminUserId, groupId);
        Ruleset saved = rulesetRepositoryPort.upsertForGroupTournament(groupId, tournamentId, rulePoints);
        predictionScoringService.recalculatePredictionScores(groupId, tournamentId, saved);
        return new RulesetResult(saved, getDisabledRules(saved.getRulePoints().keySet()));
    }

    private Map<String, Integer> getDisabledRules(Set<String> activeRuleNames) {
        return allScoringRules.stream()
                .map(ScoringRule::name)
                .filter(name -> !activeRuleNames.contains(name))
                .collect(Collectors.toMap(name -> name, name -> DEFAULT_RULE_POINTS.getOrDefault(name, 1)));
    }

    public record RulesetResult(Ruleset ruleset, Map<String, Integer> disabledRules) {}
}
