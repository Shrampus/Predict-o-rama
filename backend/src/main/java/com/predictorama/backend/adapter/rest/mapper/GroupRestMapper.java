package com.predictorama.backend.adapter.rest.mapper;

import com.predictorama.backend.adapter.rest.dto.GroupResponseDto;
import com.predictorama.backend.domain.entity.Group;

public class GroupRestMapper {

    public static GroupResponseDto toResponse(Group group) {
        return new GroupResponseDto(
                group.getId(),
                group.getOwnerId(),
                group.getInviteCode(),
                group.getName(),
                group.getDescription()
        );
    }
}
