package com.predictorama.backend.adapter.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class RulesetResponseDto {
    private UUID id;
    private Map<String, Integer> activeRules;
    private Map<String, Integer> disabledRules;
}
