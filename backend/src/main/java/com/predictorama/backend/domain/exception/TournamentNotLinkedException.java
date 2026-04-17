package com.predictorama.backend.domain.exception;

import java.util.UUID;

public class TournamentNotLinkedException extends RuntimeException {
    public TournamentNotLinkedException(UUID groupId, UUID tournamentId) {
        super("Tournament " + tournamentId + " is not linked to group " + groupId);
    }
}
