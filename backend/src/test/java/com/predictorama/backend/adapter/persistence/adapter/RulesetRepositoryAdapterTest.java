package com.predictorama.backend.adapter.persistence.adapter;

import com.predictorama.backend.adapter.persistence.entity.RulesetEntity;
import com.predictorama.backend.adapter.persistence.repository.GroupTournamentJpaRepository;
import com.predictorama.backend.adapter.persistence.repository.RulesetJpaRepository;
import com.predictorama.backend.domain.entity.Ruleset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RulesetRepositoryAdapterTest {

    @Mock
    private RulesetJpaRepository jpaRepository;

    @Mock
    private GroupTournamentJpaRepository groupTournamentJpaRepository;

    @InjectMocks
    private RulesetRepositoryAdapter adapter;

    @Test
    void findByGroupIdAndTournamentId_returnsMappedRuleset_whenRulesetIdExistsAndEntityFound() {
        UUID groupId = UUID.randomUUID();
        UUID tournamentId = UUID.randomUUID();
        UUID rulesetId = UUID.randomUUID();
        Map<String, Integer> points = Map.of("CORRECT_WINNER", 1, "EXACT_SCORE", 3);
        RulesetEntity entity = RulesetEntity.builder().id(rulesetId).rulePoints(points).build();

        when(groupTournamentJpaRepository.findRulesetIdByGroupIdAndTournamentId(groupId, tournamentId))
                .thenReturn(Optional.of(rulesetId));
        when(jpaRepository.findById(rulesetId)).thenReturn(Optional.of(entity));

        Optional<Ruleset> result = adapter.findByGroupIdAndTournamentId(groupId, tournamentId);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(rulesetId);
        assertThat(result.get().getRulePoints()).isEqualTo(points);
    }

    @Test
    void findByGroupIdAndTournamentId_returnsEmpty_whenNoRulesetIdLinked() {
        UUID groupId = UUID.randomUUID();
        UUID tournamentId = UUID.randomUUID();
        when(groupTournamentJpaRepository.findRulesetIdByGroupIdAndTournamentId(groupId, tournamentId))
                .thenReturn(Optional.empty());

        Optional<Ruleset> result = adapter.findByGroupIdAndTournamentId(groupId, tournamentId);

        assertThat(result).isEmpty();
        verifyNoInteractions(jpaRepository);
    }

    @Test
    void upsertForGroupTournament_createsNewRuleset_whenNoExistingRulesetLinked() {
        UUID groupId = UUID.randomUUID();
        UUID tournamentId = UUID.randomUUID();
        Map<String, Integer> points = Map.of("EXACT_SCORE", 5);
        when(groupTournamentJpaRepository.findRulesetIdByGroupIdAndTournamentId(groupId, tournamentId))
                .thenReturn(Optional.empty());
        when(jpaRepository.saveAndFlush(any(RulesetEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Ruleset result = adapter.upsertForGroupTournament(groupId, tournamentId, points);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getRulePoints()).isEqualTo(points);
        verify(jpaRepository).saveAndFlush(any(RulesetEntity.class));
        verify(groupTournamentJpaRepository).updateRulesetId(eq(groupId), eq(tournamentId), any(UUID.class));
    }

    @Test
    void upsertForGroupTournament_updatesExistingRuleset_whenRulesetAlreadyLinked() {
        UUID groupId = UUID.randomUUID();
        UUID tournamentId = UUID.randomUUID();
        UUID existingRulesetId = UUID.randomUUID();
        Map<String, Integer> oldPoints = Map.of("CORRECT_WINNER", 1);
        Map<String, Integer> newPoints = Map.of("CORRECT_WINNER", 3, "EXACT_SCORE", 5);
        RulesetEntity existing = RulesetEntity.builder().id(existingRulesetId).rulePoints(oldPoints).build();

        when(groupTournamentJpaRepository.findRulesetIdByGroupIdAndTournamentId(groupId, tournamentId))
                .thenReturn(Optional.of(existingRulesetId));
        when(jpaRepository.findById(existingRulesetId)).thenReturn(Optional.of(existing));

        Ruleset result = adapter.upsertForGroupTournament(groupId, tournamentId, newPoints);

        assertThat(result.getId()).isEqualTo(existingRulesetId);
        assertThat(result.getRulePoints()).isEqualTo(newPoints);
        verify(jpaRepository, never()).saveAndFlush(any());
        verify(groupTournamentJpaRepository, never()).updateRulesetId(any(), any(), any());
    }
}
