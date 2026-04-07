package com.predictorama.backend.adapter.rest.dto;

import com.predictorama.backend.domain.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserGroupsResponseDto {
    private UUID groupId;
    private UUID inviteCode;
    private String name;
    private String description;
    private Role groupMemberRole;
}
