package com.predictorama.backend.domain.service;

import org.springframework.stereotype.Service;
import com.predictorama.backend.domain.entity.Match;
import com.predictorama.backend.domain.entity.Team;
import com.predictorama.backend.domain.entity.Tournament;
import com.predictorama.backend.domain.port.external.FootballDataPort;
import com.predictorama.backend.domain.port.persistence.MatchRepositoryPort;
import com.predictorama.backend.domain.port.persistence.TeamRepositoryPort;
import com.predictorama.backend.domain.port.persistence.TournamentRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchResultSyncService {

    private final FootballDataPort footballDataPort;
    private final MatchRepositoryPort matchRepositoryPort;
    private final TeamRepositoryPort teamRepositoryPort;
    private final TournamentRepositoryPort tournamentRepositoryPort;
    private final CompetitionCatalog competitionCatalog;
    private final PredictionScoringService predictionScoringService;

    private static final Logger log = LoggerFactory.getLogger(MatchResultSyncService.class);

    private Team saveOrGetTeam(Team incomingTeam) {
        return teamRepositoryPort.findByName(incomingTeam.getName())
                .map(existingTeam -> {
                    String existingImage = existingTeam.getImageUrl();
                    String incomingImage = incomingTeam.getImageUrl();

                    boolean imageChanged = incomingImage != null &&
                            (existingImage == null || !existingImage.equals(incomingImage));

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
                                    .build());

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
}
