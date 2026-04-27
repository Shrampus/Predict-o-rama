package com.predictorama.backend.adapter.persistence.mapper;

import com.predictorama.backend.adapter.persistence.entity.RulesetEntity;
import com.predictorama.backend.domain.entity.Ruleset;

public class RulesetMapper {

    public static Ruleset toDomain(RulesetEntity rulesetEntity) {
        return Ruleset.builder()
                .id(rulesetEntity.getId())
                .rulePoints(rulesetEntity.getRulePoints())
                .build();
    }

    public static RulesetEntity toEntity(Ruleset ruleset) {
        return RulesetEntity.builder()
                .id(ruleset.getId())
                .rulePoints(ruleset.getRulePoints())
                .build();
    }
}
