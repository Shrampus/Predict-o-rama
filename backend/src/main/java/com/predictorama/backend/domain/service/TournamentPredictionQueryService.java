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
import java.util.Comparator;
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
        if (!competitionCatalog.isSupportedCompetition(competition)) {
            log.warn("Rejected unsupported competition code={} on tournament predictions request", competition);
            throw new InvalidPredictionException("Unsupported competition code: " + competition);
        }

        Tournament tournament = predictionFixtureImportService.getOrCreateTournament(competition);
        List<Match> matches = getTournamentMatches(competition, tournament);
        Tournament refreshedTournament = predictionFixtureImportService.getOrCreateTournament(competition);
        Map<UUID, Prediction> predictionsByMatchId =
                predictionService.getPredictionsByUserAndGroup(userId, groupId);

        List<TournamentMatchPredictionView> responseMatches = matches.stream()
                .sorted(Comparator.comparing(Match::getKickoffTime))
                .map(match -> toView(match, predictionsByMatchId.get(match.getId())))
                .toList();

        return new TournamentPredictionsView(
                competitionCatalog.toTournamentName(competition),
                refreshedTournament.getSeasonIdentifier(),
                resolveSeasonLabel(competition, refreshedTournament),
                competitionCatalog.toPhaseLabel(competition),
                responseMatches
        );
    }

    private TournamentMatchPredictionView toView(Match match, Prediction prediction) {
        Score primaryPredictedScore = prediction != null ? prediction.primaryPredictedScore().orElse(null) : null;
        Score actualScore = match.primaryScore().orElse(null);

        return TournamentMatchPredictionView.builder()
                .matchId(match.getId())
                .externalMatchId(match.getExternalId())
                .homeTeamName(match.getHomeTeam().getName())
                .awayTeamName(match.getAwayTeam().getName())
                .homeTeamImage(match.getHomeTeam().getImageUrl())
                .awayTeamImage(match.getAwayTeam().getImageUrl())
                .kickoffTime(match.getKickoffTime())
                .matchStatus(match.getMatchStatus().name())
                .roundIdentifier(match.getRoundIdentifier())
                .groupIdentifier(match.getGroupIdentifier())
                .matchdayIdentifier(match.getMatchdayIdentifier())
                .predictionId(prediction != null ? prediction.getId() : null)
                .predictedHomeScore(primaryPredictedScore != null ? primaryPredictedScore.getHomeScore() : null)
                .predictedAwayScore(primaryPredictedScore != null ? primaryPredictedScore.getAwayScore() : null)
                .predictedWinner(prediction != null ? prediction.getPredictedWinner() : null)
                .actualHomeScore(actualScore != null ? actualScore.getHomeScore() : null)
                .actualAwayScore(actualScore != null ? actualScore.getAwayScore() : null)
                .actualWinner(match.getWinner())
                .predictionResult(prediction != null ? prediction.getResult() : null)
                .build();
    }

    private String resolveSeasonLabel(String competition, Tournament tournament) {
        if (tournament.getSeasonLabel() != null && !tournament.getSeasonLabel().isBlank()) {
            return tournament.getSeasonLabel();
        }
        return competitionCatalog.toSeasonLabel(competition);
    }

    private List<Match> getTournamentMatches(String competition, Tournament tournament) {
        Instant now = Instant.now();
        Instant twoWeeksAgo = now.minus(14, ChronoUnit.DAYS);
        Instant in28Days = now.plus(28, ChronoUnit.DAYS);

        List<Match> matches = matchRepositoryPort.findByTournamentIdAndKickoffTimeBetween(
                tournament.getId(),
                twoWeeksAgo,
                in28Days
        );

        log.info(
                "Using matches from DB for competition={} tournament={} count={}",
                competition,
                tournament.getName(),
                matches.size()
        );

        return matches;
    }
}
