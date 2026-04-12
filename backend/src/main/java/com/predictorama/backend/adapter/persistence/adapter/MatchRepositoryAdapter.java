package com.predictorama.backend.adapter.persistence.adapter;

import com.predictorama.backend.adapter.persistence.entity.MatchEntity;
import com.predictorama.backend.adapter.persistence.entity.MatchScoreEntity;
import com.predictorama.backend.adapter.persistence.mapper.MatchMapper;
import com.predictorama.backend.adapter.persistence.mapper.MatchScoreMapper;
import com.predictorama.backend.adapter.persistence.mapper.TeamMapper;
import com.predictorama.backend.adapter.persistence.repository.MatchJpaRepository;
import com.predictorama.backend.adapter.persistence.repository.MatchScoreJpaRepository;
import com.predictorama.backend.adapter.persistence.repository.TeamJpaRepository;
import com.predictorama.backend.domain.entity.Match;
import com.predictorama.backend.domain.entity.Score;
import com.predictorama.backend.domain.entity.Team;
import com.predictorama.backend.domain.port.persistence.MatchRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
@RequiredArgsConstructor
public class MatchRepositoryAdapter implements MatchRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(MatchRepositoryAdapter.class);

    private final MatchJpaRepository jpaRepository;
    private final MatchScoreJpaRepository matchScoreRepository;
    private final TeamJpaRepository teamJpaRepository;

    @Override
    @Transactional
    public Match save(Match match) {
        log.debug("Saving match - id={}, name={}", match.getId(), match.getName());
        MatchEntity saved = jpaRepository.save(MatchMapper.toEntity(match));

        matchScoreRepository.deleteByMatchId(saved.getId());
        List<MatchScoreEntity> scoreEntities = match.getScores().stream()
                .map(score -> MatchScoreMapper.toEntity(saved.getId(), score))
                .toList();
        matchScoreRepository.saveAll(scoreEntities);

        log.debug("Match saved - id={}, scores={}", saved.getId(), scoreEntities.size());
        List<Score> scores = scoreEntities.stream()
                .map(MatchScoreMapper::toDomain)
                .toList();

        return MatchMapper.toDomain(saved, scores, match.getHomeTeam(), match.getAwayTeam());
    }

    @Override
    public Optional<Match> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toMatch);
    }

    @Override
    public List<Match> findByTournamentId(UUID tournamentId) {
        return toMatches(jpaRepository.findByTournamentId(tournamentId));
    }

    @Override
    public List<Match> findByTournamentIdAndMatchStatus(UUID tournamentId, Match.MatchStatus matchStatus) {
        return toMatches(jpaRepository.findByTournamentIdAndMatchStatus(tournamentId, matchStatus));
    }

    @Override
    public List<Match> findByTournamentIdAndKickoffTimeBetween(UUID tournamentId, Instant from, Instant to) {
        return toMatches(jpaRepository.findByTournamentIdAndKickoffTimeBetween(tournamentId, from, to));
    }

    @Override
    public List<Match> findByKickoffTimeBetween(Instant from, Instant to) {
        return toMatches(jpaRepository.findByKickoffTimeBetween(from, to));
    }

    @Override
    public Optional<Match> findByExternalId(String externalId) {
        return jpaRepository.findByExternalId(externalId).map(this::toMatch);
    }

    private Match toMatch(MatchEntity entity) {
        return toMatches(List.of(entity)).get(0);
    }

    private List<Match> toMatches(List<MatchEntity> entities) {
        if (entities.isEmpty()) {
            return List.of();
        }

        Map<UUID, Team> teamsById = loadTeamsById(entities);
        Map<UUID, List<Score>> scoresByMatchId = loadScoresByMatchId(entities);

        return entities.stream()
                .map(entity -> MatchMapper.toDomain(
                        entity,
                        scoresByMatchId.getOrDefault(entity.getId(), List.of()),
                        requireTeam(teamsById, entity.getHomeTeamId()),
                        requireTeam(teamsById, entity.getAwayTeamId())
                ))
                .toList();
    }

    private Map<UUID, Team> loadTeamsById(List<MatchEntity> entities) {
        List<UUID> teamIds = entities.stream()
                .flatMap(entity -> Stream.of(entity.getHomeTeamId(), entity.getAwayTeamId()))
                .distinct()
                .toList();

        return teamJpaRepository.findAllById(teamIds).stream()
                .map(TeamMapper::toDomain)
                .collect(Collectors.toMap(Team::getId, Function.identity()));
    }

    private Map<UUID, List<Score>> loadScoresByMatchId(List<MatchEntity> entities) {
        List<UUID> matchIds = entities.stream()
                .map(MatchEntity::getId)
                .toList();

        return matchScoreRepository.findByMatchIdIn(matchIds).stream()
                .collect(Collectors.groupingBy(
                        MatchScoreEntity::getMatchId,
                        Collectors.mapping(MatchScoreMapper::toDomain, Collectors.toList())
                ));
    }

    private Team requireTeam(Map<UUID, Team> teamsById, UUID teamId) {
        Team team = teamsById.get(teamId);
        if (team == null) {
            throw new IllegalStateException("Team not found: " + teamId);
        }
        return team;
    }
}