package com.predictorama.backend.adapter.persistence.adapter;

import com.predictorama.backend.adapter.persistence.mapper.RulesetMapper;
import com.predictorama.backend.adapter.persistence.repository.RulesetJpaRepository;
import com.predictorama.backend.domain.entity.Ruleset;
import com.predictorama.backend.domain.port.persistence.RulesetRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RulesetRepositoryAdapter implements RulesetRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(RulesetRepositoryAdapter.class);

    private final RulesetJpaRepository jpaRepository;

    @Override
    public Ruleset save(Ruleset ruleset) {
        log.debug("Saving ruleset - id={}, name={}", ruleset.getId(), ruleset.getName());
        Ruleset saved = RulesetMapper.toDomain(jpaRepository.save(RulesetMapper.toEntity(ruleset)));
        log.debug("Ruleset saved - id={}", saved.getId());
        return saved;
    }

    @Override
    public Optional<Ruleset> findById(UUID id) {
        log.debug("Finding ruleset by id - id={}", id);
        return jpaRepository.findById(id).map(RulesetMapper::toDomain);
    }

    @Override
    public Optional<Ruleset> findByName(String name) {
        log.debug("Finding ruleset by name - name={}", name);
        return jpaRepository.findByName(name).map(RulesetMapper::toDomain);
    }

    @Override
    public List<Ruleset> findAll() {
        log.debug("Finding all rulesets");
        return jpaRepository.findAll().stream().map(RulesetMapper::toDomain).toList();
    }
}
