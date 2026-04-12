package com.predictorama.backend.adapter.persistence.adapter;

import com.predictorama.backend.adapter.persistence.mapper.TeamMapper;
import com.predictorama.backend.adapter.persistence.repository.TeamJpaRepository;
import com.predictorama.backend.domain.entity.Team;
import com.predictorama.backend.domain.port.persistence.TeamRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TeamRepositoryAdapter implements TeamRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(TeamRepositoryAdapter.class);

    private final TeamJpaRepository jpaRepository;

    @Override
    public Team save(Team team) {
        log.debug("Saving team - id={}, name={}", team.getId(), team.getName());
        Team saved = TeamMapper.toDomain(jpaRepository.save(TeamMapper.toEntity(team)));
        log.debug("Team saved - id={}", saved.getId());
        return saved;
    }

    @Override
    public Optional<Team> findById(UUID id) {
        return jpaRepository.findById(id).map(TeamMapper::toDomain);
    }

    @Override
    public List<Team> findAll() {
        return jpaRepository.findAll().stream().map(TeamMapper::toDomain).toList();
    }

    @Override
    public Optional<Team> findByName(String name) {
        return jpaRepository.findByName(name).map(TeamMapper::toDomain);
    }
}
