package com.predictorama.backend.domain.exception;

import java.util.UUID;

public class GroupMemberNotFoundException extends RuntimeException {
    public GroupMemberNotFoundException(UUID groupId, UUID userId) {
        super("User " + userId + " is not a member of group " + groupId);
    }
}
