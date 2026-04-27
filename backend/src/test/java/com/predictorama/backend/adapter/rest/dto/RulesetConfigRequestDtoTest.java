package com.predictorama.backend.adapter.rest.dto;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RulesetConfigRequestDtoTest {

    private RulesetConfigRequestDto dto(Map<String, Integer> rulePoints) {
        RulesetConfigRequestDto d = new RulesetConfigRequestDto();
        d.setRulePoints(rulePoints);
        return d;
    }

    @Test
    void isRulePointsValid_returnsTrue_whenAllValuesArePositive() {
        assertThat(dto(Map.of("CORRECT_WINNER", 1, "EXACT_SCORE", 3)).isRulePointsValid()).isTrue();
    }

    @Test
    void isRulePointsValid_returnsFalse_whenAnyValueIsZero() {
        assertThat(dto(Map.of("CORRECT_WINNER", 0)).isRulePointsValid()).isFalse();
    }

    @Test
    void isRulePointsValid_returnsFalse_whenAnyValueIsNegative() {
        assertThat(dto(Map.of("CORRECT_WINNER", -1)).isRulePointsValid()).isFalse();
    }

    @Test
    void isRulePointsValid_returnsFalse_whenAnyValueIsNull() {
        Map<String, Integer> points = new HashMap<>();
        points.put("CORRECT_WINNER", null);
        assertThat(dto(points).isRulePointsValid()).isFalse();
    }

    @Test
    void isRulePointsValid_returnsTrue_whenRulePointsIsNull() {
        assertThat(dto(null).isRulePointsValid()).isTrue();
    }
}
