package com.predictorama.backend.domain.exception;

import java.util.UUID;

public class GroupAccessDeniedException extends RuntimeException {
    public GroupAccessDeniedException(UUID userId, UUID groupId) {
        super("User " + userId + " is not allowed to manage group " + groupId);
    }
}
