package com.predictorama.backend.domain.service;

import org.springframework.stereotype.Component;

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

    public boolean isSupportedCompetition(String competition) {
        return competition != null && SUPPORTED_COMPETITIONS.contains(competition);
    }

    public String toTournamentName(String competition) {
        return COMPETITION_NAME_BY_CODE.getOrDefault(competition, competition);
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

    public String toCompetitionCode(String tournamentName) {
        return switch (tournamentName) {
            case "FIFA World Cup" -> "WC";
            case "UEFA Champions League" -> "CL";
            case "Bundesliga" -> "BL1";
            case "Eredivisie" -> "DED";
            case "Campeonato Brasileiro Série A" -> "BSA";
            case "Primera Division" -> "PD";
            case "Ligue 1" -> "FL1";
            case "Championship" -> "ELC";
            case "Primeira Liga" -> "PPL";
            case "UEFA European Championship" -> "EC";
            case "Serie A" -> "SA";
            case "Premier League" -> "PL";
            default -> tournamentName;
        };
    }
}