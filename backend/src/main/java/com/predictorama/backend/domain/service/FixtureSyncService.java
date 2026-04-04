package com.predictorama.backend.domain.service;

import com.predictorama.backend.config.FixtureSyncProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FixtureSyncService {

    private static final Logger log = LoggerFactory.getLogger(FixtureSyncService.class);

    private final FixtureSyncProperties fixtureSyncProperties;
    private final CompetitionCatalog competitionCatalog;
    private final PredictionFixtureImportService predictionFixtureImportService;

    private int currentCompetitionIndex = 0;

    public synchronized void syncNextCompetition() {
        List<String> orderedCompetitions = getOrderedConfiguredCompetitions();

        if (orderedCompetitions.isEmpty()) {
            log.info("Scheduled fixture sync skipped because no valid competitions are configured");
            return;
        }

        if (currentCompetitionIndex >= orderedCompetitions.size()) {
            currentCompetitionIndex = 0;
        }

        String competition = orderedCompetitions.get(currentCompetitionIndex);
        currentCompetitionIndex = (currentCompetitionIndex + 1) % orderedCompetitions.size();

        try {
            log.info("Starting scheduled fixture sync for competition={}", competition);

            int importedCount = predictionFixtureImportService.importUpcomingMatches(competition).size();

            log.info(
                    "Finished scheduled fixture sync for competition={} importedCount={}",
                    competition,
                    importedCount
            );
        } catch (Exception e) {
            log.error("Scheduled fixture sync failed for competition={}", competition, e);
        }
    }

    public List<String> getOrderedConfiguredCompetitions() {
        List<String> configuredCompetitions = fixtureSyncProperties.getCompetitions();

        if (configuredCompetitions == null || configuredCompetitions.isEmpty()) {
            return List.of();
        }

        Set<String> normalizedSupportedCompetitions = new LinkedHashSet<>();

        for (String competition : configuredCompetitions) {
            String normalizedCompetition = normalizeCompetitionCode(competition);

            if (normalizedCompetition == null) {
                continue;
            }

            if (!competitionCatalog.isSupportedCompetition(normalizedCompetition)) {
                log.warn(
                        "Ignoring unsupported configured fixture sync competition code={}",
                        normalizedCompetition
                );
                continue;
            }

            normalizedSupportedCompetitions.add(normalizedCompetition);
        }

        List<String> orderedCompetitions = new ArrayList<>();

        addIfPresent(normalizedSupportedCompetitions, orderedCompetitions, "WC");
        addIfPresent(normalizedSupportedCompetitions, orderedCompetitions, "EC");
        addIfPresent(normalizedSupportedCompetitions, orderedCompetitions, "CL");
        addIfPresent(normalizedSupportedCompetitions, orderedCompetitions, "PL");

        for (String competition : normalizedSupportedCompetitions) {
            if (!orderedCompetitions.contains(competition)) {
                orderedCompetitions.add(competition);
            }
        }

        return orderedCompetitions;
    }

    private void addIfPresent(Set<String> availableCompetitions, List<String> orderedCompetitions, String competition) {
        if (availableCompetitions.contains(competition)) {
            orderedCompetitions.add(competition);
        }
    }

    private String normalizeCompetitionCode(String competition) {
        if (competition == null) {
            return null;
        }

        String trimmedCompetition = competition.trim();
        if (trimmedCompetition.isEmpty()) {
            return null;
        }

        return trimmedCompetition.toUpperCase(Locale.ROOT);
    }
}