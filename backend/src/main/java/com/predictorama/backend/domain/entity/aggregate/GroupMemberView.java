package com.predictorama.backend.domain.entity.aggregate;

import com.predictorama.backend.domain.entity.GroupMember;

public record GroupMemberView(GroupMember member, String username) {
}
