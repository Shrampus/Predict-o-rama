package com.predictorama.backend.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class GroupMember {
    private UUID id;
    private UUID userId;
    private UUID groupId;
    private Role memberRole;
    private MemberStatus status;

    public enum MemberStatus{
        ACTIVE, INACTIVE
    }
}
