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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PredictionPageService {

    private static final Logger log = LoggerFactory.getLogger(PredictionPageService.class);

    private final FootballDataPort footballDataPort;
    private final MatchRepositoryPort matchRepositoryPort;
    private final TeamRepositoryPort teamRepositoryPort;
    private final TournamentRepositoryPort tournamentRepositoryPort;

    public List<Match> getPredictionPageMatches(String competition) {
        Tournament tournament = getOrCreateTournament(competition);

        Instant now = Instant.now();
        Instant in28Days = now.plus(28, ChronoUnit.DAYS);

        List<Match> existingMatches = matchRepositoryPort.findByTournamentId(tournament.getId()).stream()
                .filter(match -> match.getKickoffTime() != null)
                .filter(match -> !match.getKickoffTime().isBefore(now))
                .filter(match -> !match.getKickoffTime().isAfter(in28Days))
                .toList();

        if (!existingMatches.isEmpty()) {
            log.info(
                    "Using cached matches from DB for competition={} tournament={} count={}",
                    competition,
                    tournament.getName(),
                    existingMatches.size()
            );
            return existingMatches;
        }

        log.info(
                "Fetching matches from football-data API for competition={} tournament={}",
                competition,
                tournament.getName()
        );

        List<Match> externalMatches = footballDataPort.getUpcomingMatches(competition);

        List<Match> savedMatches = externalMatches.stream()
                .map(match -> saveOrUpdateMatch(match, tournament))
                .toList();

        log.info(
                "Fetched and saved matches from API for competition={} tournament={} count={}",
                competition,
                tournament.getName(),
                savedMatches.size()
        );

        return savedMatches;
    }

    private Tournament getOrCreateTournament(String competition) {
        String tournamentName = mapCompetitionToTournamentName(competition);

        return tournamentRepositoryPort.findAll().stream()
                .filter(tournament -> tournament.getName().equalsIgnoreCase(tournamentName))
                .findFirst()
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

    private Team saveOrGetTeam(Team incomingTeam) {
        return teamRepositoryPort.findByName(incomingTeam.getName())
                .map(existingTeam -> {
                    String existingImage = existingTeam.getImageUrl();
                    String incomingImage = incomingTeam.getImageUrl();

                    boolean imageChanged =
                            incomingImage != null &&
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

    private String mapCompetitionToTournamentName(String competition) {
        return switch (competition) {
            case "WC" -> "FIFA World Cup";
            case "CL" -> "UEFA Champions League";
            case "BL1" -> "Bundesliga";
            case "DED" -> "Eredivisie";
            case "BSA" -> "Campeonato Brasileiro Série A";
            case "PD" -> "Primera Division";
            case "FL1" -> "Ligue 1";
            case "ELC" -> "Championship";
            case "PPL" -> "Primeira Liga";
            case "EC" -> "UEFA European Championship";
            case "SA" -> "Serie A";
            case "PL" -> "Premier League";
            default -> competition;
        };
    }
}