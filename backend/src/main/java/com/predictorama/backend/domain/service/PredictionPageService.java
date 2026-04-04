package com.predictorama.backend.domain.service;

import com.predictorama.backend.domain.entity.Match;
import com.predictorama.backend.domain.entity.Tournament;
import com.predictorama.backend.domain.port.persistence.MatchRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PredictionPageService {

    private static final Logger log = LoggerFactory.getLogger(PredictionPageService.class);

    private final MatchRepositoryPort matchRepositoryPort;
    private final PredictionFixtureImportService predictionFixtureImportService;
    private final CompetitionCatalog competitionCatalog;

    public List<Match> getPredictionPageMatches(String competition) {
        Tournament tournament = predictionFixtureImportService.getOrCreateTournament(competition);

        Instant now = Instant.now();
        Instant in28Days = now.plus(28, ChronoUnit.DAYS);

        List<Match> existingMatches = matchRepositoryPort.findByTournamentIdAndKickoffTimeBetween(
                tournament.getId(),
                now,
                in28Days
        );

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
                competitionCatalog.toTournamentName(competition)
        );

        List<Match> savedMatches = predictionFixtureImportService.importUpcomingMatches(competition);

        log.info(
                "Fetched and saved matches from API for competition={} tournament={} count={}",
                competition,
                competitionCatalog.toTournamentName(competition),
                savedMatches.size()
        );

        return savedMatches;
    }
}