package com.predictorama.backend.domain.service;

import org.springframework.stereotype.Component;

@Component
public class CompetitionCatalog {

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
}