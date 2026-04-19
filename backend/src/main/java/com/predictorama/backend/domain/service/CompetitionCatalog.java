package com.predictorama.backend.domain.service;

import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.Map;
import java.util.Set;

@Component
public class CompetitionCatalog {

    private static final Map<String, String> COMPETITION_NAME_BY_CODE = Map.ofEntries(
            Map.entry("WC", "FIFA World Cup"),
            Map.entry("CL", "UEFA Champions League"),
            Map.entry("BL1", "Bundesliga"),
            Map.entry("DED", "Eredivisie"),
            Map.entry("BSA", "Campeonato Brasileiro Série A"),
            Map.entry("PD", "Primera Division"),
            Map.entry("FL1", "Ligue 1"),
            Map.entry("ELC", "Championship"),
            Map.entry("PPL", "Primeira Liga"),
            Map.entry("EC", "UEFA European Championship"),
            Map.entry("SA", "Serie A"),
            Map.entry("PL", "Premier League")
    );

    private static final Set<String> SUPPORTED_COMPETITIONS = COMPETITION_NAME_BY_CODE.keySet();
    private static final Set<String> INTERNATIONAL_TOURNAMENTS = Set.of("WC", "EC");
    private static final Set<String> LEAGUE_PHASE_TOURNAMENTS = Set.of("CL");

    public boolean isSupportedCompetition(String competition) {
        return competition != null && SUPPORTED_COMPETITIONS.contains(competition);
    }

    public String toTournamentName(String competition) {
        return COMPETITION_NAME_BY_CODE.getOrDefault(competition, competition);
    }

    public String toSeasonLabel(String competition) {
        int currentYear = Year.now().getValue();
        if (INTERNATIONAL_TOURNAMENTS.contains(competition)) {
            return currentYear + " Tournament";
        }

        int nextYearShort = (currentYear + 1) % 100;
        return currentYear + "/" + String.format("%02d", nextYearShort) + " Season";
    }

    public String toPhaseLabel(String competition) {
        if (INTERNATIONAL_TOURNAMENTS.contains(competition)) {
            return "Group Stage";
        }

        if (LEAGUE_PHASE_TOURNAMENTS.contains(competition)) {
            return "League Phase";
        }

        return "Regular Season";
    }

    public String toCompetitionCode(String tournamentName) {
        if (tournamentName == null) {
            return null;
        }

        return COMPETITION_NAME_BY_CODE.entrySet().stream()
                .filter(entry -> entry.getValue().equals(tournamentName))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    public Set<String> getSupportedCompetitions() {
        return SUPPORTED_COMPETITIONS;
    }
}