package com.predictorama.backend.domain.service;

import com.predictorama.backend.domain.entity.Match;
import com.predictorama.backend.domain.entity.Team;
import com.predictorama.backend.domain.entity.Tournament;
import com.predictorama.backend.domain.port.external.FootballDataPort;
import com.predictorama.backend.domain.port.persistence.MatchRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchResultSyncService {

    private final PredictionFixtureImportService predictionFixtureImportService;
    private final FootballDataPort footballDataPort;
    private final MatchRepositoryPort matchRepositoryPort;
    private final TeamSyncService teamSyncService;
    private final CompetitionCatalog competitionCatalog;
    private final PredictionScoringService predictionScoringService;

    private static final Logger log = LoggerFactory.getLogger(MatchResultSyncService.class);

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
                            .scores(externalMatch.getScores())
                            .winner(externalMatch.getWinner())
                            .externalId(existingMatch.getExternalId())
                            .build();

                    Match savedMatch = matchRepositoryPort.save(updatedMatch);
                    log.debug("Updated match in DB externalId={} localId={}", savedMatch.getExternalId(),
                            savedMatch.getId());
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
                            .scores(externalMatch.getScores())
                            .winner(externalMatch.getWinner())
                            .externalId(externalMatch.getExternalId())
                            .build();

                    Match savedMatch = matchRepositoryPort.save(newMatch);
                    log.debug("Created match in DB externalId={} localId={}", savedMatch.getExternalId(),
                            savedMatch.getId());
                    return savedMatch;
                });

    }

    private String buildMatchName(Team homeTeam, Team awayTeam) {
        return homeTeam.getName() + " vs " + awayTeam.getName();
    }

    private boolean isValidFinishedMatch(Match match) {
        if (match == null) {
            log.warn("Skipping finished match because it is null");
            return false;
        }

        if (isBlank(match.getExternalId())) {
            log.warn("Skipping finished match because externalId is missing");
            return false;
        }

        if (match.getHomeTeam() == null) {
            log.warn("Skipping finished match externalId={} because homeTeam is missing", match.getExternalId());
            return false;
        }

        if (match.getAwayTeam() == null) {
            log.warn("Skipping finished match externalId={} because awayTeam is missing", match.getExternalId());
            return false;
        }

        if (isBlank(match.getHomeTeam().getName())) {
            log.warn("Skipping finished match externalId={} because homeTeam.name is missing", match.getExternalId());
            return false;
        }

        if (isBlank(match.getHomeTeam().getExternalId())) {
            log.warn("Skipping finished match externalId={} because homeTeam.externalId is missing", match.getExternalId());
            return false;
        }

        if (isBlank(match.getAwayTeam().getName())) {
            log.warn("Skipping finished match externalId={} because awayTeam.name is missing", match.getExternalId());
            return false;
        }

        if (isBlank(match.getAwayTeam().getExternalId())) {
            log.warn("Skipping finished match externalId={} because awayTeam.externalId is missing", match.getExternalId());
            return false;
        }

        return true;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public void syncAllCompetitions() {
        for (String competition : competitionCatalog.getSupportedCompetitions()) {
            syncCompetition(competition);
        }
    }

    private void syncCompetition(String competition) {
        var finishedMatches = footballDataPort.getFinishedMatches(competition);

        if (finishedMatches.isEmpty()) {
            log.info("No finished matches to sync for competition={}", competition);
            return;
        }

        Tournament tournament = predictionFixtureImportService.getOrCreateTournament(competition);

        for (Match match : finishedMatches) {
            if (!isValidFinishedMatch(match)) {
                continue;
            }

            try {
                Match savedMatch = saveOrUpdateMatch(match, tournament);
                log.info("Synced match result for matchId={} externalId={} competition={}",
                        savedMatch.getId(), savedMatch.getExternalId(), competition);
                predictionScoringService.distributePredictionScores(savedMatch.getId());
            } catch (Exception e) {
                log.error(
                        "Failed to sync finished match externalId={} competition={}",
                        match.getExternalId(),
                        competition,
                        e
                );
            }
        }
    }
}
