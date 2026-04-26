package com.predictorama.backend.adapter.rest.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class RulesetConfigRequestDto {
    private Map<String, Integer> rulePoints;
}
