package com.predictorama.backend.domain.service;

import com.predictorama.backend.domain.entity.Match;
import com.predictorama.backend.domain.entity.Score;
import com.predictorama.backend.domain.entity.Team;
import com.predictorama.backend.domain.entity.Tournament;
import com.predictorama.backend.domain.entity.Winner;
import com.predictorama.backend.domain.port.external.FootballDataPort;
import com.predictorama.backend.domain.port.persistence.MatchRepositoryPort;
import com.predictorama.backend.domain.port.persistence.TournamentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PredictionFixtureImportService {

    private static final Logger log = LoggerFactory.getLogger(PredictionFixtureImportService.class);

    private final FootballDataPort footballDataPort;
    private final MatchRepositoryPort matchRepositoryPort;
    private final TeamSyncService teamSyncService;
    private final TournamentRepositoryPort tournamentRepositoryPort;
    private final CompetitionCatalog competitionCatalog;

    public List<Match> importUpcomingMatches(String competition) {
        return importMatches(competition, LocalDate.now(), LocalDate.now().plusDays(28));
    }

    public List<Match> importMatches(String competition, LocalDate dateFrom, LocalDate dateTo) {
        if (!competitionCatalog.isSupportedCompetition(competition)) {
            log.warn("Rejected fixture import for unsupported competition code={}", competition);
            return List.of();
        }

        List<Match> externalMatches = footballDataPort.getMatches(competition, dateFrom, dateTo);
        Tournament tournament = getOrCreateTournament(competition);
        Tournament tournamentWithSeasonIdentifier = withSeasonIdentifier(tournament, externalMatches);

        return externalMatches.stream()
                .filter(this::isValidExternalMatch)
                .map(match -> saveOrUpdateMatch(match, tournamentWithSeasonIdentifier))
                .toList();
    }

    public Tournament getOrCreateTournament(String competition) {
        if (!competitionCatalog.isSupportedCompetition(competition)) {
            throw new IllegalArgumentException("Unsupported competition code: " + competition);
        }

        String tournamentName = competitionCatalog.toTournamentName(competition);

        return tournamentRepositoryPort.findByNameIgnoreCase(tournamentName)
        .orElseGet(() -> {
            Tournament savedTournament = tournamentRepositoryPort.save(
                    Tournament.builder()
                            .id(UUID.randomUUID())
                            .name(tournamentName)
                            .sport(Tournament.Sport.FOOTBALL)
                            .build()
            );

            log.info("Created tournament in DB name={} id={}", savedTournament.getName(), savedTournament.getId());
            return savedTournament;
        });
    }

    private boolean isValidExternalMatch(Match match) {
        if (match == null) {
            log.warn("Skipping imported match because it is null");
            return false;
        }

        if (isBlank(match.getExternalId())) {
            log.warn("Skipping imported match because externalId is missing");
            return false;
        }

        if (match.getKickoffTime() == null) {
            log.warn("Skipping imported match externalId={} because kickoffTime is missing", match.getExternalId());
            return false;
        }

        if (match.getHomeTeam() == null) {
            log.warn("Skipping imported match externalId={} because homeTeam is missing", match.getExternalId());
            return false;
        }

        if (match.getAwayTeam() == null) {
            log.warn("Skipping imported match externalId={} because awayTeam is missing", match.getExternalId());
            return false;
        }

        if (isBlank(match.getHomeTeam().getName())) {
            log.warn("Skipping imported match externalId={} because homeTeam.name is missing", match.getExternalId());
            return false;
        }

        if (isBlank(match.getHomeTeam().getExternalId())) {
            log.warn("Skipping imported match externalId={} because homeTeam.externalId is missing", match.getExternalId());
            return false;
        }

        if (isBlank(match.getAwayTeam().getName())) {
            log.warn("Skipping imported match externalId={} because awayTeam.name is missing", match.getExternalId());
            return false;
        }

        if (isBlank(match.getAwayTeam().getExternalId())) {
            log.warn("Skipping imported match externalId={} because awayTeam.externalId is missing", match.getExternalId());
            return false;
        }

        return true;
    }

    private Match saveOrUpdateMatch(Match externalMatch, Tournament tournament) {
        Team savedHomeTeam = teamSyncService.saveOrGetTeam(externalMatch.getHomeTeam());
        Team savedAwayTeam = teamSyncService.saveOrGetTeam(externalMatch.getAwayTeam());

        return matchRepositoryPort.findByExternalId(externalMatch.getExternalId())
                .map(existingMatch -> {
                    Match updatedMatch = Match.builder()
                            .id(existingMatch.getId())
                            .tournamentId(existingMatch.getTournamentId())
                            .name(buildMatchName(savedHomeTeam, savedAwayTeam))
                            .description(existingMatch.getDescription())
                            .homeTeam(savedHomeTeam)
                            .awayTeam(savedAwayTeam)
                            .matchStatus(externalMatch.getMatchStatus())
                            .kickoffTime(externalMatch.getKickoffTime())
                            .seasonIdentifier(existingMatch.getSeasonIdentifier())
                            .roundIdentifier(externalMatch.getRoundIdentifier())
                            .groupIdentifier(externalMatch.getGroupIdentifier())
                            .matchdayIdentifier(externalMatch.getMatchdayIdentifier())
                            .scores(resolveScores(existingMatch, externalMatch))
                            .winner(resolveWinner(existingMatch, externalMatch))
                            .externalId(existingMatch.getExternalId())
                            .build();

                    Match savedMatch = matchRepositoryPort.save(updatedMatch);
                    log.debug("Updated match in DB externalId={} localId={}", savedMatch.getExternalId(), savedMatch.getId());
                    return savedMatch;
                })
                .orElseGet(() -> {
                    Match newMatch = Match.builder()
                            .id(UUID.randomUUID())
                            .tournamentId(tournament.getId())
                            .name(buildMatchName(savedHomeTeam, savedAwayTeam))
                            .description(null)
                            .homeTeam(savedHomeTeam)
                            .awayTeam(savedAwayTeam)
                            .matchStatus(externalMatch.getMatchStatus())
                            .kickoffTime(externalMatch.getKickoffTime())
                            .seasonIdentifier(externalMatch.getSeasonIdentifier())
                            .roundIdentifier(externalMatch.getRoundIdentifier())
                            .groupIdentifier(externalMatch.getGroupIdentifier())
                            .matchdayIdentifier(externalMatch.getMatchdayIdentifier())
                            .scores(externalMatch.getScores() == null ? List.of() : externalMatch.getScores())
                            .winner(externalMatch.getWinner())
                            .externalId(externalMatch.getExternalId())
                            .build();

                    Match savedMatch = matchRepositoryPort.save(newMatch);
                    log.debug("Created match in DB externalId={} localId={}", savedMatch.getExternalId(), savedMatch.getId());
                    return savedMatch;
                });
    }

    private String buildMatchName(Team homeTeam, Team awayTeam) {
        return homeTeam.getName() + " vs " + awayTeam.getName();
    }

    private Tournament withSeasonIdentifier(Tournament tournament, List<Match> externalMatches) {
        String seasonIdentifier = externalMatches.stream()
                .map(Match::getSeasonIdentifier)
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElse(null);
        String seasonLabel = externalMatches.stream()
                .map(Match::getSeasonLabel)
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElse(null);

        boolean seasonIdentifierChanged = seasonIdentifier != null && !seasonIdentifier.equals(tournament.getSeasonIdentifier());
        boolean seasonLabelChanged = seasonLabel != null && !seasonLabel.equals(tournament.getSeasonLabel());

        if (!seasonIdentifierChanged && !seasonLabelChanged) {
            return tournament;
        }

        Tournament updatedTournament = Tournament.builder()
                .id(tournament.getId())
                .name(tournament.getName())
                .seasonLabel(seasonLabel != null ? seasonLabel : tournament.getSeasonLabel())
                .seasonIdentifier(seasonIdentifier != null ? seasonIdentifier : tournament.getSeasonIdentifier())
                .sport(tournament.getSport())
                .build();

        Tournament savedTournament = tournamentRepositoryPort.save(updatedTournament);
        log.info(
                "Updated tournament season metadata name={} seasonIdentifier={} seasonLabel={}",
                savedTournament.getName(),
                savedTournament.getSeasonIdentifier(),
                savedTournament.getSeasonLabel()
        );
        return savedTournament;
    }

    private List<Score> resolveScores(Match existingMatch, Match externalMatch) {
        if (externalMatch.getScores() == null || externalMatch.getScores().isEmpty()) {
            return existingMatch.getScores();
        }

        return externalMatch.getScores();
    }

    private Winner resolveWinner(Match existingMatch, Match externalMatch) {
        if (externalMatch.getWinner() == null) {
            return existingMatch.getWinner();
        }

        return externalMatch.getWinner();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
