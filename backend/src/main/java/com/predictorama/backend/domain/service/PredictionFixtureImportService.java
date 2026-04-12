package com.predictorama.backend.domain.service;

import com.predictorama.backend.domain.entity.Match;
import com.predictorama.backend.domain.entity.Team;
import com.predictorama.backend.domain.entity.Tournament;
import com.predictorama.backend.domain.port.external.FootballDataPort;
import com.predictorama.backend.domain.port.persistence.MatchRepositoryPort;
import com.predictorama.backend.domain.port.persistence.TeamRepositoryPort;
import com.predictorama.backend.domain.port.persistence.TournamentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PredictionFixtureImportService {

    private static final Logger log = LoggerFactory.getLogger(PredictionFixtureImportService.class);

    private final FootballDataPort footballDataPort;
    private final MatchRepositoryPort matchRepositoryPort;
    private final TeamRepositoryPort teamRepositoryPort;
    private final TournamentRepositoryPort tournamentRepositoryPort;
    private final CompetitionCatalog competitionCatalog;

    public List<Match> importUpcomingMatches(String competition) {
        if (!competitionCatalog.isSupportedCompetition(competition)) {
            log.warn("Rejected fixture import for unsupported competition code={}", competition);
            return List.of();
        }

        Tournament tournament = getOrCreateTournament(competition);

        return footballDataPort.getUpcomingMatches(competition).stream()
                .filter(this::isValidExternalMatch)
                .map(match -> saveOrUpdateMatch(match, tournament))
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
                            .description("Imported from football-data API")
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

        if (isBlank(match.getAwayTeam().getName())) {
            log.warn("Skipping imported match externalId={} because awayTeam.name is missing", match.getExternalId());
            return false;
        }

        return true;
    }

    private Team saveOrGetTeam(Team incomingTeam) {
        if (incomingTeam == null || isBlank(incomingTeam.getName())) {
            throw new IllegalArgumentException("Cannot save team with missing name");
        }

        return teamRepositoryPort.findByName(incomingTeam.getName())
                .map(existingTeam -> {
                    String existingImage = existingTeam.getImageUrl();
                    String incomingImage = incomingTeam.getImageUrl();

                    boolean imageChanged =
                            incomingImage != null &&
                            !Objects.equals(existingImage, incomingImage);

                    if (!imageChanged) {
                        return existingTeam;
                    }

                    Team updatedTeam = Team.builder()
                            .id(existingTeam.getId())
                            .name(existingTeam.getName())
                            .imageUrl(incomingImage)
                            .build();

                    Team savedTeam = teamRepositoryPort.save(updatedTeam);
                    log.info("Updated team image in DB name={} id={}", savedTeam.getName(), savedTeam.getId());
                    return savedTeam;
                })
                .orElseGet(() -> {
                    Team savedTeam = teamRepositoryPort.save(
                            Team.builder()
                                    .id(UUID.randomUUID())
                                    .name(incomingTeam.getName())
                                    .imageUrl(incomingTeam.getImageUrl())
                                    .build()
                    );

                    log.info("Created team in DB name={} id={}", savedTeam.getName(), savedTeam.getId());
                    return savedTeam;
                });
    }

    private Match saveOrUpdateMatch(Match externalMatch, Tournament tournament) {
        Team savedHomeTeam = saveOrGetTeam(externalMatch.getHomeTeam());
        Team savedAwayTeam = saveOrGetTeam(externalMatch.getAwayTeam());

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
                            .scores(existingMatch.getScores())
                            .winner(existingMatch.getWinner())
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
                            .scores(List.of())
                            .winner(null)
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}