package com.predictorama.backend.domain.service;

import com.predictorama.backend.domain.entity.Match;
import com.predictorama.backend.domain.entity.Score;
import com.predictorama.backend.domain.entity.Team;
import com.predictorama.backend.domain.entity.Tournament;
import com.predictorama.backend.domain.entity.Winner;
import com.predictorama.backend.domain.port.external.FootballDataPort;
import com.predictorama.backend.domain.port.persistence.MatchRepositoryPort;
import com.predictorama.backend.domain.port.persistence.TeamRepositoryPort;
import com.predictorama.backend.domain.port.persistence.TournamentRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PredictionFixtureImportServiceTest {

    private StubFootballDataPort footballDataPort;
    private InMemoryMatchRepository matchRepository;
    private InMemoryTournamentRepository tournamentRepository;
    private InMemoryTeamRepository teamRepository;
    private PredictionFixtureImportService service;

    @BeforeEach
    void setUp() {
        footballDataPort = new StubFootballDataPort();
        matchRepository = new InMemoryMatchRepository();
        tournamentRepository = new InMemoryTournamentRepository();
        teamRepository = new InMemoryTeamRepository();

        TeamSyncService teamSyncService = new TeamSyncService(teamRepository);
        CompetitionCatalog competitionCatalog = new CompetitionCatalog();

        service = new PredictionFixtureImportService(
                footballDataPort,
                matchRepository,
                teamSyncService,
                tournamentRepository,
                competitionCatalog
        );
    }

    @Test
    void importMatches_updatesExistingMatchWithCompletedResult() {
        Tournament tournament = tournamentRepository.save(Tournament.builder()
                .id(UUID.randomUUID())
                .name("UEFA Champions League")
                .description("Imported")
                .sport(Tournament.Sport.FOOTBALL)
                .build());

        Team homeTeam = teamRepository.save(Team.builder()
                .id(UUID.randomUUID())
                .name("Home FC")
                .externalId("10")
                .imageUrl("home.png")
                .build());
        Team awayTeam = teamRepository.save(Team.builder()
                .id(UUID.randomUUID())
                .name("Away FC")
                .externalId("20")
                .imageUrl("away.png")
                .build());

        Match existingMatch = matchRepository.save(Match.builder()
                .id(UUID.randomUUID())
                .tournamentId(tournament.getId())
                .name("Home FC vs Away FC")
                .description(null)
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .matchStatus(Match.MatchStatus.SCHEDULED)
                .kickoffTime(Instant.parse("2026-04-20T18:00:00Z"))
                .scores(List.of())
                .winner(null)
                .externalId("match-1")
                .build());

        footballDataPort.matchesToReturn = List.of(Match.builder()
                .id(null)
                .tournamentId(null)
                .name("Home FC vs Away FC")
                .description(null)
                .homeTeam(Team.builder().name("Home FC").externalId("10").imageUrl("home.png").build())
                .awayTeam(Team.builder().name("Away FC").externalId("20").imageUrl("away.png").build())
                .matchStatus(Match.MatchStatus.COMPLETED)
                .kickoffTime(existingMatch.getKickoffTime())
                .seasonIdentifier("777")
                .seasonLabel("2025/26")
                .scores(List.of(Score.builder()
                        .homeScore(2)
                        .awayScore(1)
                        .scoreType(Score.ScoreType.FULL_TIME)
                        .build()))
                .winner(Winner.HOME)
                .externalId("match-1")
                .build());

        List<Match> importedMatches = service.importMatches("CL", LocalDate.of(2026, 4, 19), LocalDate.of(2026, 4, 21));

        assertThat(importedMatches).hasSize(1);
        Match importedMatch = importedMatches.getFirst();
        assertThat(importedMatch.getId()).isEqualTo(existingMatch.getId());
        assertThat(importedMatch.getMatchStatus()).isEqualTo(Match.MatchStatus.COMPLETED);
        assertThat(importedMatch.getWinner()).isEqualTo(Winner.HOME);
        assertThat(importedMatch.getScores()).hasSize(1);
        assertThat(importedMatch.getScores().getFirst().getHomeScore()).isEqualTo(2);
        assertThat(importedMatch.getScores().getFirst().getAwayScore()).isEqualTo(1);
        assertThat(tournamentRepository.findById(tournament.getId())).get().extracting(Tournament::getDescription).isEqualTo("2025/26");
    }

    @Test
    void importMatches_persistsScoresWhenCompletedMatchIsCreatedFromLookbackWindow() {
        footballDataPort.matchesToReturn = List.of(Match.builder()
                .id(null)
                .tournamentId(null)
                .name("Fresh Home vs Fresh Away")
                .description(null)
                .homeTeam(Team.builder().name("Fresh Home").externalId("30").imageUrl("home.png").build())
                .awayTeam(Team.builder().name("Fresh Away").externalId("40").imageUrl("away.png").build())
                .matchStatus(Match.MatchStatus.COMPLETED)
                .kickoffTime(Instant.parse("2026-04-20T20:00:00Z"))
                .seasonIdentifier("777")
                .seasonLabel("2025/26")
                .scores(List.of(Score.builder()
                        .homeScore(3)
                        .awayScore(2)
                        .scoreType(Score.ScoreType.FULL_TIME)
                        .build()))
                .winner(Winner.HOME)
                .externalId("match-2")
                .build());

        List<Match> importedMatches = service.importMatches("CL", LocalDate.of(2026, 4, 19), LocalDate.of(2026, 4, 21));

        assertThat(importedMatches).hasSize(1);
        Match importedMatch = importedMatches.getFirst();
        assertThat(importedMatch.getMatchStatus()).isEqualTo(Match.MatchStatus.COMPLETED);
        assertThat(importedMatch.getWinner()).isEqualTo(Winner.HOME);
        assertThat(importedMatch.getScores()).hasSize(1);
        assertThat(importedMatch.getScores().getFirst().getHomeScore()).isEqualTo(3);
        assertThat(importedMatch.getScores().getFirst().getAwayScore()).isEqualTo(2);
        assertThat(tournamentRepository.findAll()).singleElement().extracting(Tournament::getDescription).isEqualTo("2025/26");
    }

    private static final class StubFootballDataPort implements FootballDataPort {
        private List<Match> matchesToReturn = List.of();

        @Override
        public List<Match> getMatches(String competition, LocalDate dateFrom, LocalDate dateTo) {
            return matchesToReturn;
        }
    }

    private static final class InMemoryMatchRepository implements MatchRepositoryPort {
        private final Map<UUID, Match> store = new HashMap<>();

        @Override
        public Match save(Match match) {
            store.put(match.getId(), match);
            return match;
        }

        @Override
        public Optional<Match> findById(UUID id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public List<Match> findByTournamentId(UUID tournamentId) {
            return store.values().stream()
                    .filter(match -> tournamentId.equals(match.getTournamentId()))
                    .toList();
        }

        @Override
        public List<Match> findByTournamentIdAndMatchStatus(UUID tournamentId, Match.MatchStatus matchStatus) {
            return store.values().stream()
                    .filter(match -> tournamentId.equals(match.getTournamentId()) && matchStatus == match.getMatchStatus())
                    .toList();
        }

        @Override
        public List<Match> findByTournamentIdAndKickoffTimeBetween(UUID tournamentId, Instant from, Instant to) {
            return store.values().stream()
                    .filter(match -> tournamentId.equals(match.getTournamentId()))
                    .filter(match -> !match.getKickoffTime().isBefore(from) && !match.getKickoffTime().isAfter(to))
                    .toList();
        }

        @Override
        public List<Match> findByKickoffTimeBetween(Instant from, Instant to) {
            return store.values().stream()
                    .filter(match -> !match.getKickoffTime().isBefore(from) && !match.getKickoffTime().isAfter(to))
                    .toList();
        }

        @Override
        public Optional<Match> findByExternalId(String externalId) {
            return store.values().stream()
                    .filter(match -> externalId.equals(match.getExternalId()))
                    .findFirst();
        }
    }

    private static final class InMemoryTournamentRepository implements TournamentRepositoryPort {
        private final Map<UUID, Tournament> store = new HashMap<>();

        @Override
        public Tournament save(Tournament tournament) {
            store.put(tournament.getId(), tournament);
            return tournament;
        }

        @Override
        public Optional<Tournament> findById(UUID id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public Optional<Tournament> findByNameIgnoreCase(String name) {
            return store.values().stream()
                    .filter(tournament -> tournament.getName().equalsIgnoreCase(name))
                    .findFirst();
        }

        @Override
        public List<Tournament> findAll() {
            return List.copyOf(store.values());
        }
    }

    private static final class InMemoryTeamRepository implements TeamRepositoryPort {
        private final Map<UUID, Team> byId = new HashMap<>();
        private final Map<String, UUID> byExternalId = new HashMap<>();

        @Override
        public Team save(Team team) {
            byId.put(team.getId(), team);
            if (team.getExternalId() != null) {
                byExternalId.put(team.getExternalId(), team.getId());
            }
            return team;
        }

        @Override
        public Optional<Team> findById(UUID id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public Optional<Team> findByExternalId(String externalId) {
            UUID id = byExternalId.get(externalId);
            return id == null ? Optional.empty() : Optional.ofNullable(byId.get(id));
        }

        @Override
        public List<Team> findAll() {
            return List.copyOf(byId.values());
        }
    }
}
