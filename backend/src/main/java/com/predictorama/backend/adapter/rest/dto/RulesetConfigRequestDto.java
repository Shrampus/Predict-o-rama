package com.predictorama.backend.adapter.rest.dto;

import lombok.Getter;

import java.util.Map;

@Getter
public class RulesetConfigRequestDto {
    private Map<String, Integer> rulePoints;
}
