package com.predictorama.backend.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;
import java.util.Set;

@Getter
@Builder
@AllArgsConstructor
public class Ruleset{
    private UUID id;
    private String name;
    private Set<String> ruleNames;

}
