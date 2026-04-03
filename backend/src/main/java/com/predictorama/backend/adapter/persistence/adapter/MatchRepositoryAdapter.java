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
import java.util.Optional;
import java.util.UUID;

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
        return jpaRepository.findByTournamentId(tournamentId).stream()
                .map(this::toMatch)
                .toList();
    }

    @Override
    public List<Match> findByTournamentIdAndMatchStatus(UUID tournamentId, Match.MatchStatus matchStatus) {
        return jpaRepository.findByTournamentIdAndMatchStatus(tournamentId, matchStatus).stream()
                .map(this::toMatch)
                .toList();
    }

    @Override
    public List<Match> findByTournamentIdAndKickoffTimeBetween(UUID tournamentId, Instant from, Instant to) {
        return jpaRepository.findByTournamentId(tournamentId).stream()
                .filter(entity -> entity.getKickoffTime() != null)
                .filter(entity -> !entity.getKickoffTime().isBefore(from))
                .filter(entity -> !entity.getKickoffTime().isAfter(to))
                .map(this::toMatch)
                .toList();
    }

    @Override
    public List<Match> findByKickoffTimeBetween(Instant from, Instant to) {
        return jpaRepository.findByKickoffTimeBetween(from, to).stream()
                .map(this::toMatch)
                .toList();
    }

    @Override
    public Optional<Match> findByExternalId(String externalId) {
        return jpaRepository.findByExternalId(externalId).map(this::toMatch);
    }

    private Match toMatch(MatchEntity entity) {
        List<Score> scores = loadScores(entity.getId());
        Team homeTeam = loadTeam(entity.getHomeTeamId());
        Team awayTeam = loadTeam(entity.getAwayTeamId());
        return MatchMapper.toDomain(entity, scores, homeTeam, awayTeam);
    }

    private List<Score> loadScores(UUID matchId) {
        return matchScoreRepository.findByMatchId(matchId).stream()
                .map(MatchScoreMapper::toDomain)
                .toList();
    }

    private Team loadTeam(UUID teamId) {
        return teamJpaRepository.findById(teamId)
                .map(TeamMapper::toDomain)
                .orElseThrow(() -> new IllegalStateException("Team not found: " + teamId));
    }
}