package com.predictorama.backend.domain.service;

import com.predictorama.backend.domain.entity.Match;
import com.predictorama.backend.domain.entity.Prediction;
import com.predictorama.backend.domain.entity.Score;
import com.predictorama.backend.domain.entity.Tournament;
import com.predictorama.backend.domain.exception.InvalidPredictionException;
import com.predictorama.backend.domain.port.persistence.MatchRepositoryPort;
import com.predictorama.backend.domain.query.TournamentMatchPredictionView;
import com.predictorama.backend.domain.query.TournamentPredictionsView;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TournamentPredictionQueryService {

    private static final Logger log = LoggerFactory.getLogger(TournamentPredictionQueryService.class);

    private final MatchRepositoryPort matchRepositoryPort;
    private final PredictionFixtureImportService predictionFixtureImportService;
    private final CompetitionCatalog competitionCatalog;
    private final PredictionService predictionService;

    public TournamentPredictionsView getTournamentPredictions(
            String competition,
            UUID userId,
            UUID groupId
    ) {
        List<Match> matches = getTournamentMatches(competition);
        Map<UUID, Prediction> predictionsByMatchId =
                predictionService.getPredictionsByUserAndGroup(userId, groupId);

        List<TournamentMatchPredictionView> responseMatches = matches.stream()
                .map(match -> toView(match, predictionsByMatchId.get(match.getId())))
                .toList();

        return new TournamentPredictionsView(
                competitionCatalog.toTournamentName(competition),
                responseMatches
        );
    }

    private TournamentMatchPredictionView toView(Match match, Prediction prediction) {
        Score primaryPredictedScore = prediction != null ? prediction.primaryPredictedScore().orElse(null) : null;

        return TournamentMatchPredictionView.builder()
                .matchId(match.getId())
                .externalMatchId(match.getExternalId())
                .homeTeamName(match.getHomeTeam().getName())
                .awayTeamName(match.getAwayTeam().getName())
                .homeTeamImage(match.getHomeTeam().getImageUrl())
                .awayTeamImage(match.getAwayTeam().getImageUrl())
                .kickoffTime(match.getKickoffTime())
                .matchStatus(match.getMatchStatus().name())
                .predictionId(prediction != null ? prediction.getId() : null)
                .predictedHomeScore(primaryPredictedScore != null ? primaryPredictedScore.getHomeScore() : null)
                .predictedAwayScore(primaryPredictedScore != null ? primaryPredictedScore.getAwayScore() : null)
                .predictedWinner(prediction != null ? prediction.getPredictedWinner() : null)
                .build();
    }

    private List<Match> getTournamentMatches(String competition) {
        if (!competitionCatalog.isSupportedCompetition(competition)) {
            log.warn("Rejected unsupported competition code={} on tournament predictions request", competition);
            throw new InvalidPredictionException("Unsupported competition code: " + competition);
        }

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