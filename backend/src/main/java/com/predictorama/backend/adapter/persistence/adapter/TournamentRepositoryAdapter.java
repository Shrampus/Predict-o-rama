package com.predictorama.backend.adapter.persistence.adapter;

import com.predictorama.backend.adapter.persistence.mapper.TournamentMapper;
import com.predictorama.backend.adapter.persistence.repository.TournamentJpaRepository;
import com.predictorama.backend.domain.entity.Tournament;
import com.predictorama.backend.domain.port.persistence.TournamentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TournamentRepositoryAdapter implements TournamentRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(TournamentRepositoryAdapter.class);

    private final TournamentJpaRepository jpaRepository;

    @Override
    public Tournament save(Tournament tournament) {
        log.debug("Saving tournament - id={}, name={}", tournament.getId(), tournament.getName());
        Tournament saved = TournamentMapper.toDomain(jpaRepository.save(TournamentMapper.toEntity(tournament)));
        log.debug("Tournament saved - id={}", saved.getId());
        return saved;
    }

    @Override
    public Optional<Tournament> findById(UUID id) {
        return jpaRepository.findById(id).map(TournamentMapper::toDomain);
    }

    @Override
    public Optional<Tournament> findByNameIgnoreCase(String name) {
        return jpaRepository.findByNameIgnoreCase(name)
                .map(TournamentMapper::toDomain);
    }

    @Override
    public List<Tournament> findAll() {
        return jpaRepository.findAll().stream().map(TournamentMapper::toDomain).toList();
    }
}
