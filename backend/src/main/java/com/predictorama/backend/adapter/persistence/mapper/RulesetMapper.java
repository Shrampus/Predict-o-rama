package com.predictorama.backend.adapter.persistence.mapper;

import com.predictorama.backend.adapter.persistence.entity.RulesetEntity;
import com.predictorama.backend.domain.entity.Ruleset;

public class RulesetMapper {

    public static Ruleset toDomain(RulesetEntity rulesetEntity) {
        return Ruleset.builder()
                .id(rulesetEntity.getId())
                .name(rulesetEntity.getName())
                .ruleNames(rulesetEntity.getRuleNames())
                .build();
    }

    public static RulesetEntity toEntity(Ruleset ruleset) {
        return RulesetEntity.builder()
                .id(ruleset.getId())
                .name(ruleset.getName())
                .ruleNames(ruleset.getRuleNames())
                .build();
    }
}
