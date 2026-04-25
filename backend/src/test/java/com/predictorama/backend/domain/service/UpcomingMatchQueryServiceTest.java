package com.predictorama.backend.domain.service;

import com.predictorama.backend.domain.entity.Match;
import com.predictorama.backend.domain.entity.Team;
import com.predictorama.backend.domain.port.persistence.MatchRepositoryPort;
import com.predictorama.backend.domain.service.result.UpcomingMatchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UpcomingMatchQueryServiceTest {

    private UpcomingMatchQueryService service;
    private InMemoryMatchRepository matchRepository;

    @BeforeEach
    void setUp() {
        matchRepository = new InMemoryMatchRepository();
        service = new UpcomingMatchQueryService(matchRepository);
    }

    @Test
    void getGenericUpcomingMatches_returnsMatchesWithinNextWindow() {
        matchRepository.add(matchWithKickoff(Instant.now().plus(3, ChronoUnit.DAYS)));
        matchRepository.add(matchWithKickoff(Instant.now().plus(10, ChronoUnit.DAYS)));
        matchRepository.add(matchWithKickoff(Instant.now().plus(30, ChronoUnit.DAYS)));

        List<UpcomingMatchResult> results = service.getGenericUpcomingMatches();

        assertThat(results).hasSize(2);
        assertThat(results).allMatch(r -> r.getUserGroups().isEmpty());
    }

    @Test
    void getGenericUpcomingMatches_excludesMatchesBeyond28Days() {
        matchRepository.add(matchWithKickoff(Instant.now().plus(28, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS)));

        assertThat(service.getGenericUpcomingMatches()).isEmpty();
    }

    // --- Helpers ---

    private Match matchWithKickoff(Instant kickoff) {
        Team team = Team.builder().id(UUID.randomUUID()).name("Team").imageUrl("").build();
        return Match.builder()
                .id(UUID.randomUUID())
                .tournamentId(UUID.randomUUID())
                .homeTeam(team)
                .awayTeam(team)
                .matchStatus(Match.MatchStatus.SCHEDULED)
                .kickoffTime(kickoff)
                .build();
    }

    // --- In-memory stub ---

    static class InMemoryMatchRepository implements MatchRepositoryPort {
        private final List<Match> store = new ArrayList<>();

        void add(Match match) { store.add(match); }

        @Override
        public Match save(Match match) { store.add(match); return match; }

        @Override
        public Optional<Match> findById(UUID id) {
            return store.stream().filter(m -> m.getId().equals(id)).findFirst();
        }

        @Override
        public List<Match> findByTournamentId(UUID tournamentId) {
            return store.stream().filter(m -> m.getTournamentId().equals(tournamentId)).toList();
        }

        @Override
        public List<Match> findByTournamentIdAndMatchStatus(UUID tournamentId, Match.MatchStatus status) {
            return store.stream()
                    .filter(m -> m.getTournamentId().equals(tournamentId) && m.getMatchStatus() == status)
                    .toList();
        }

        @Override
        public List<Match> findByTournamentIdAndKickoffTimeBetween(UUID tournamentId, Instant from, Instant to) {
            return store.stream()
                    .filter(m -> m.getTournamentId().equals(tournamentId)
                            && !m.getKickoffTime().isBefore(from)
                            && m.getKickoffTime().isBefore(to))
                    .toList();
        }

        @Override
        public List<Match> findByKickoffTimeBetween(Instant from, Instant to) {
            return store.stream()
                    .filter(m -> !m.getKickoffTime().isBefore(from) && m.getKickoffTime().isBefore(to))
                    .toList();
        }

        @Override
        public Optional<Match> findByExternalId(String externalId) { return Optional.empty(); }

        @Override
        public List<Match> findAllFinishedByTournamentId(UUID tournamentId) {
            return findByTournamentIdAndMatchStatus(tournamentId, Match.MatchStatus.COMPLETED);
        }
    }
}
