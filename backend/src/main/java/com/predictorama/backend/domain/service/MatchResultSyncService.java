package com.predictorama.backend.domain.service;

import com.predictorama.backend.config.ResultSyncProperties;
import com.predictorama.backend.domain.entity.Match;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchResultSyncService {

    private static final Logger log = LoggerFactory.getLogger(MatchResultSyncService.class);

    private final PredictionFixtureImportService predictionFixtureImportService;
    private final CompetitionCatalog competitionCatalog;
    private final PredictionScoringService predictionScoringService;
    private final ResultSyncProperties resultSyncProperties;

    public void syncAllCompetitions() {
        for (String competition : competitionCatalog.getSupportedCompetitions()) {
            syncCompetition(competition);
        }
    }

    private void syncCompetition(String competition) {
        LocalDate dateFrom = LocalDate.now().minusDays(resultSyncProperties.getLookbackDays());
        LocalDate dateTo = LocalDate.now();
        List<Match> recentMatches = predictionFixtureImportService.importMatches(competition, dateFrom, dateTo);

        if (recentMatches.isEmpty()) {
            log.info("No recent matches to sync for competition={} dateFrom={} dateTo={}", competition, dateFrom, dateTo);
            return;
        }

        for (Match match : recentMatches) {
            if (match.getMatchStatus() != Match.MatchStatus.COMPLETED) {
                continue;
            }

            try {
                predictionScoringService.distributePredictionScores(match.getId());
                log.info("Synced match result for matchId={} externalId={} competition={}",
                        match.getId(), match.getExternalId(), competition);
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
