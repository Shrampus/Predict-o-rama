package com.predictorama.backend.domain.service;

import com.predictorama.backend.domain.entity.Match;
import com.predictorama.backend.domain.entity.Tournament;
import com.predictorama.backend.domain.port.persistence.MatchRepositoryPort;
import com.predictorama.backend.domain.port.persistence.TournamentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PredictionPageService {

    private static final Logger log = LoggerFactory.getLogger(PredictionPageService.class);

    private final MatchRepositoryPort matchRepositoryPort;
    private final TournamentRepositoryPort tournamentRepositoryPort;
    private final CompetitionCatalog competitionCatalog;

    public List<Match> getPredictionPageMatches(String competition) {
        if (!competitionCatalog.isSupportedCompetition(competition)) {
            log.warn("Rejected unsupported competition code={} on prediction page request", competition);
            return List.of();
        }

        String tournamentName = competitionCatalog.toTournamentName(competition);

        Optional<Tournament> tournament = tournamentRepositoryPort.findAll().stream()
                .filter(existingTournament -> existingTournament.getName().equalsIgnoreCase(tournamentName))
                .findFirst();

        if (tournament.isEmpty()) {
            log.info(
                    "No tournament found in DB for competition={} tournament={}; returning empty result",
                    competition,
                    tournamentName
            );
            return List.of();
        }

        Instant now = Instant.now();
        Instant in28Days = now.plus(28, ChronoUnit.DAYS);

        List<Match> matches = matchRepositoryPort.findByTournamentIdAndKickoffTimeBetween(
                tournament.get().getId(),
                now,
                in28Days
        );

        log.info(
                "Loaded matches from DB for competition={} tournament={} count={}",
                competition,
                tournamentName,
                matches.size()
        );

        return matches;
    }
}