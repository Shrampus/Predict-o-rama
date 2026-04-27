package com.predictorama.backend.adapter.rest.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class RulesetConfigRequestDto {
    @NotNull
    @NotEmpty
    private Map<String, Integer> rulePoints;

    @AssertTrue(message = "All point values must be positive integers")
    public boolean isRulePointsValid() {
        if (rulePoints == null) return true;
        return rulePoints.values().stream().allMatch(v -> v != null && v >= 1);
    }
}
