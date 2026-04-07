package com.predictorama.backend.adapter.rest.mapper;

import com.predictorama.backend.adapter.rest.dto.GroupMemberResponseDto;
import com.predictorama.backend.domain.entity.GroupMember;

public class GroupMemberMapper {

    public static GroupMemberResponseDto toResponse(GroupMember member) {
        return new GroupMemberResponseDto(
                member.getId(),
                member.getGroupId(),
                member.getUserId(),
                member.getMemberRole(),
                member.getStatus()
        );
    }
}
