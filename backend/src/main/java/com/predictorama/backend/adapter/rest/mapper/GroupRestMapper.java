package com.predictorama.backend.adapter.rest.mapper;

import com.predictorama.backend.adapter.rest.dto.GroupResponse;
import com.predictorama.backend.domain.entity.Group;

public class GroupRestMapper {

    public static GroupResponse toResponse(Group group) {
        return new GroupResponse(
                group.getId(),
                group.getOwnerId(),
                group.getInviteCode(),
                group.getName(),
                group.getDescription()
        );
    }
}
