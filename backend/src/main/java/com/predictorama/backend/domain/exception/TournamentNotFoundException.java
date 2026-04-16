package com.predictorama.backend.domain.exception;

import java.util.UUID;

public class TournamentNotFoundException extends RuntimeException {
    public TournamentNotFoundException(UUID tournamentId) {
        super("Tournament not found: " + tournamentId);
    }
}
