package com.predictorama.backend.adapter.rest;

import com.predictorama.backend.adapter.rest.controller.RulesetController;
import com.predictorama.backend.adapter.rest.dto.RulesetConfigRequestDto;
import com.predictorama.backend.adapter.rest.dto.RulesetResponseDto;
import com.predictorama.backend.domain.entity.Ruleset;
import com.predictorama.backend.domain.exception.TournamentNotLinkedException;
import com.predictorama.backend.domain.service.RulesetService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RulesetControllerTest {

    @Mock
    private RulesetService rulesetService;

    @InjectMocks
    private RulesetController controller;

    private final UUID userId = UUID.randomUUID();
    private final UUID groupId = UUID.randomUUID();
    private final UUID tournamentId = UUID.randomUUID();

    @BeforeEach
    void setUpSecurityContext() {
        var auth = new UsernamePasswordAuthenticationToken(userId.toString(), null, List.of());
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getRuleset_returns200WithRulesetDto() {
        Map<String, Integer> active = Map.of("CORRECT_WINNER", 1, "EXACT_SCORE", 3);
        Map<String, Integer> disabled = Map.of("CORRECT_GOAL_DIFFERENCE", 2);
        Ruleset ruleset = Ruleset.builder().id(UUID.randomUUID()).rulePoints(active).build();
        when(rulesetService.getRuleset(userId, groupId, tournamentId))
                .thenReturn(new RulesetService.RulesetResult(ruleset, disabled));

        ResponseEntity<RulesetResponseDto> response = controller.getRuleset(
                groupId, tournamentId, new MockHttpServletRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getActiveRules()).isEqualTo(active);
        assertThat(response.getBody().getDisabledRules()).isEqualTo(disabled);
    }

    @Test
    void getRuleset_propagatesTournamentNotLinked_whenServiceThrows() {
        when(rulesetService.getRuleset(userId, groupId, tournamentId))
                .thenThrow(new TournamentNotLinkedException(groupId, tournamentId));

        assertThatThrownBy(() -> controller.getRuleset(groupId, tournamentId, new MockHttpServletRequest()))
                .isInstanceOf(TournamentNotLinkedException.class);
    }

    @Test
    void updateRuleset_returns200WithUpdatedDto() {
        Map<String, Integer> newPoints = Map.of("EXACT_SCORE", 5);
        RulesetConfigRequestDto request = new RulesetConfigRequestDto();
        request.setRulePoints(newPoints);
        Ruleset saved = Ruleset.builder().id(UUID.randomUUID()).rulePoints(newPoints).build();
        when(rulesetService.updateRuleset(userId, groupId, tournamentId, newPoints))
                .thenReturn(new RulesetService.RulesetResult(saved, Map.of("CORRECT_WINNER", 1)));

        ResponseEntity<RulesetResponseDto> response = controller.updateRuleset(
                groupId, tournamentId, request, new MockHttpServletRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getActiveRules()).isEqualTo(newPoints);
        verify(rulesetService).updateRuleset(userId, groupId, tournamentId, newPoints);
    }
}
