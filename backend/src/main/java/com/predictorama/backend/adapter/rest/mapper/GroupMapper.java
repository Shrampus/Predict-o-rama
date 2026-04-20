package com.predictorama.backend.adapter.rest.mapper;

import com.predictorama.backend.adapter.rest.dto.GroupResponseDto;
import com.predictorama.backend.adapter.rest.dto.UserGroupsResponseDto;
import com.predictorama.backend.domain.entity.Group;
import com.predictorama.backend.domain.entity.aggregate.UserGroups;

public class GroupMapper {

    public static GroupResponseDto toResponse(Group group) {
        return new GroupResponseDto(
                group.getId(),
                group.getOwnerId(),
                group.getInviteCode(),
                group.getName(),
                group.getDescription()
        );
    }

    public static UserGroupsResponseDto toUserGroupsResponse(UserGroups userGroups) {
        return new UserGroupsResponseDto(
                userGroups.getGroup().getId(),
                userGroups.getGroup().getInviteCode(),
                userGroups.getGroup().getName(),
                userGroups.getGroup().getDescription(),
                userGroups.getMembership().getMemberRole()
        );
    }
}
