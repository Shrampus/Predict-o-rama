package com.predictorama.backend.adapter.persistence.adapter;

import com.predictorama.backend.adapter.persistence.entity.RulesetEntity;
import com.predictorama.backend.adapter.persistence.mapper.RulesetMapper;
import com.predictorama.backend.adapter.persistence.repository.GroupTournamentJpaRepository;
import com.predictorama.backend.adapter.persistence.repository.RulesetJpaRepository;
import com.predictorama.backend.domain.entity.Ruleset;
import com.predictorama.backend.domain.port.persistence.RulesetRepositoryPort;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RulesetRepositoryAdapter implements RulesetRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(RulesetRepositoryAdapter.class);

    private final RulesetJpaRepository jpaRepository;
    private final GroupTournamentJpaRepository groupTournamentJpaRepository;

    @Override
    public Optional<Ruleset> findByGroupIdAndTournamentId(UUID groupId, UUID tournamentId) {
        return groupTournamentJpaRepository
                .findRulesetIdByGroupIdAndTournamentId(groupId, tournamentId)
                .flatMap(jpaRepository::findById)
                .map(RulesetMapper::toDomain);
    }

    @Transactional
    @Override
    public Ruleset upsertForGroupTournament(UUID groupId, UUID tournamentId,  Map<String, Integer> rulePoints) {
        log.debug("Upserting ruleset - groupId={}, tournamentId={}", groupId, tournamentId);

        Optional<UUID> rulesetId = groupTournamentJpaRepository.findRulesetIdByGroupIdAndTournamentId(groupId, tournamentId);

        RulesetEntity entity;
        if (rulesetId.isPresent()) {
            entity = jpaRepository.findById(rulesetId.get()).orElseThrow();
            entity.setRulePoints(rulePoints);
        } else {
            entity = RulesetEntity.builder()
                    .id(UUID.randomUUID())
                    .rulePoints(rulePoints)
                    .build();
            jpaRepository.save(entity);
            groupTournamentJpaRepository.updateRulesetId(groupId, tournamentId, entity.getId());
        }

        return RulesetMapper.toDomain(entity);
    }
}
