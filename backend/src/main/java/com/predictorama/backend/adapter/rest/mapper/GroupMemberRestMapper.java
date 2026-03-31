package com.predictorama.backend.adapter.rest.mapper;

import com.predictorama.backend.adapter.rest.dto.GroupMemberResponse;
import com.predictorama.backend.domain.entity.GroupMember;

public class GroupMemberRestMapper {

    public static GroupMemberResponse toResponse(GroupMember member) {
        return new GroupMemberResponse(
                member.getId(),
                member.getGroupId(),
                member.getUserId(),
                member.getMemberRole(),
                member.getStatus()
        );
    }
}
