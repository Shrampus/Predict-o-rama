package com.predictorama.backend.adapter.persistence.mapper;

import com.predictorama.backend.adapter.persistence.entity.GroupEntity;
import com.predictorama.backend.domain.entity.Group;

public class GroupMapper {

    public static Group toDomain(GroupEntity entity) {
        return Group.builder()
                .id(entity.getId())
                .ownerId(entity.getOwnerId())
                .inviteCode(entity.getInviteCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .rulesetId(entity.getRulesetId())
                .build();
    }

    public static GroupEntity toEntity(Group group) {
        return GroupEntity.builder()
                .id(group.getId())
                .ownerId(group.getOwnerId())
                .inviteCode(group.getInviteCode())
                .name(group.getName())
                .description(group.getDescription())
                .rulesetId(group.getRulesetId())
                .build();
    }
}
