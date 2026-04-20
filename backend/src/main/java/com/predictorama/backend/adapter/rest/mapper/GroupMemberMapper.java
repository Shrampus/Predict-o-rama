package com.predictorama.backend.adapter.rest.mapper;

import com.predictorama.backend.adapter.rest.dto.GroupMemberResponseDto;
import com.predictorama.backend.domain.entity.GroupMember;

public class GroupMemberMapper {

    public static GroupMemberResponseDto toResponse(GroupMember member) {
        return toResponse(member, member.getUserId().toString());
    }

    public static GroupMemberResponseDto toResponse(GroupMember member, String name) {
        return new GroupMemberResponseDto(
                member.getId(),
                member.getGroupId(),
                member.getUserId(),
                name,
                member.getMemberRole(),
                member.getStatus()
        );
    }
}
