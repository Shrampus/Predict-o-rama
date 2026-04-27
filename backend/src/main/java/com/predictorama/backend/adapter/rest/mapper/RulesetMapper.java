package com.predictorama.backend.adapter.rest.mapper;

import com.predictorama.backend.adapter.rest.dto.RulesetResponseDto;
import com.predictorama.backend.domain.entity.Ruleset;

import java.util.Map;

public class RulesetMapper {
    public static RulesetResponseDto toResponse(Ruleset ruleset, Map<String, Integer> disabledRules) {
        return new RulesetResponseDto(
                ruleset.getId(),
                ruleset.getRulePoints(),
                disabledRules
        );
    }
}
