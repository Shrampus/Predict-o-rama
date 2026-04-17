package com.predictorama.backend.domain.exception;

import java.util.UUID;

public class TournamentAlreadyLinkedException extends RuntimeException {
    public TournamentAlreadyLinkedException(UUID groupId, UUID tournamentId) {
        super("Tournament " + tournamentId + " is already linked to this group (" + groupId + ")");
    }
}
