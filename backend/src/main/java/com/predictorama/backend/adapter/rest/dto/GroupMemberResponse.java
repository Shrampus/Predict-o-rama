package com.predictorama.backend.adapter.rest.dto;

import com.predictorama.backend.domain.entity.GroupMember;
import com.predictorama.backend.domain.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class GroupMemberResponse {
    private UUID id;
    private UUID groupId;
    private UUID userId;
    private Role memberRole;
    private GroupMember.MemberStatus status;
}
