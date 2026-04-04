package com.predictorama.backend.adapter.persistence.mapper;

import com.predictorama.backend.adapter.persistence.entity.GroupMemberEntity;
import com.predictorama.backend.domain.entity.GroupMember;

public class GroupMemberMapper {

    public static GroupMember toDomain(GroupMemberEntity entity) {
        return GroupMember.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .groupId(entity.getGroupId())
                .status(entity.getStatus())
                .memberRole(entity.getMemberRole())
                .build();
    }

    public static GroupMemberEntity toEntity(GroupMember member) {
        return GroupMemberEntity.builder()
                .id(member.getId())
                .userId(member.getUserId())
                .groupId(member.getGroupId())
                .status(member.getStatus())
                .memberRole(member.getMemberRole())
                .build();
    }
}
