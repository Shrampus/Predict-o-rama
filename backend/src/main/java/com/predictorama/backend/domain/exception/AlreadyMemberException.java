package com.predictorama.backend.domain.exception;

import java.util.UUID;

public class AlreadyMemberException extends RuntimeException {
    public AlreadyMemberException(UUID userId, UUID groupId) {
        super("User " + userId + " is already a member of group " + groupId);
    }
}
