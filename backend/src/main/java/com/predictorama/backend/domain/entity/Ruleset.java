package com.predictorama.backend.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;
import java.util.UUID;
import java.util.Set;

@Getter
@Builder
@AllArgsConstructor
public class Ruleset {
    private UUID id;
    private Map<String, Integer> rulePoints;

}
