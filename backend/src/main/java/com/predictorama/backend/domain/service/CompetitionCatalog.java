package com.predictorama.backend.domain.service;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class CompetitionCatalog {

    private static final Set<String> SUPPORTED_COMPETITIONS = Set.of(
            "WC",
            "CL",
            "BL1",
            "DED",
            "BSA",
            "PD",
            "FL1",
            "ELC",
            "PPL",
            "EC",
            "SA",
            "PL"
    );

    public boolean isSupportedCompetition(String competition) {
        return competition != null && SUPPORTED_COMPETITIONS.contains(competition);
    }

    public String toTournamentName(String competition) {
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

    public Set<String> getSupportedCompetitions() {
        return SUPPORTED_COMPETITIONS;
    }
}