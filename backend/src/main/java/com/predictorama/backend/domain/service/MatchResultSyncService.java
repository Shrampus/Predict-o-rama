package com.predictorama.backend.domain.service;

import com.predictorama.backend.domain.entity.Match;
import com.predictorama.backend.domain.entity.Team;
import com.predictorama.backend.domain.entity.Tournament;
import com.predictorama.backend.domain.port.external.FootballDataPort;
import com.predictorama.backend.domain.port.persistence.MatchRepositoryPort;
import com.predictorama.backend.domain.port.persistence.TeamRepositoryPort;
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
    private final TeamRepositoryPort teamRepositoryPort;
    private final CompetitionCatalog competitionCatalog;
    private final PredictionScoringService predictionScoringService;

    private static final Logger log = LoggerFactory.getLogger(MatchResultSyncService.class);

    private Team saveOrGetTeam(Team incomingTeam) {
        if (incomingTeam == null || isBlank(incomingTeam.getExternalId())) {
            throw new IllegalArgumentException("Cannot save team with missing externalId");
        }

        if (isBlank(incomingTeam.getName())) {
            throw new IllegalArgumentException("Cannot save team with missing name");
        }

        return teamRepositoryPort.findByExternalId(incomingTeam.getExternalId())
                .map(existingTeam -> {
                    boolean teamChanged =
                            !incomingTeam.getName().equals(existingTeam.getName()) ||
                            (incomingTeam.getImageUrl() != null &&
                                    !incomingTeam.getImageUrl().equals(existingTeam.getImageUrl()));

                    if (!teamChanged) {
                        return existingTeam;
                    }

                    Team updatedTeam = Team.builder()
                            .id(existingTeam.getId())
                            .externalId(existingTeam.getExternalId())
                            .name(incomingTeam.getName())
                            .imageUrl(incomingTeam.getImageUrl())
                            .build();

                    Team savedTeam = teamRepositoryPort.save(updatedTeam);
                    log.info(
                            "Updated team in DB externalId={} name={} id={}",
                            savedTeam.getExternalId(),
                            savedTeam.getName(),
                            savedTeam.getId()
                    );
                    return savedTeam;
                })
                .orElseGet(() -> {
                    Team savedTeam = teamRepositoryPort.save(
                            Team.builder()
                                    .id(UUID.randomUUID())
                                    .externalId(incomingTeam.getExternalId())
                                    .name(incomingTeam.getName())
                                    .imageUrl(incomingTeam.getImageUrl())
                                    .build());

                    log.info(
                            "Created team in DB externalId={} name={} id={}",
                            savedTeam.getExternalId(),
                            savedTeam.getName(),
                            savedTeam.getId()
                    );
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
                            .scores(List.of())
                            .winner(null)
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
            Match savedMatch = saveOrUpdateMatch(match, tournament);
            log.info("Synced match result for matchId={} externalId={} competition={}",
                    savedMatch.getId(), savedMatch.getExternalId(), competition);
            predictionScoringService.distributePredictionScores(savedMatch.getId());
        }
    }
}
