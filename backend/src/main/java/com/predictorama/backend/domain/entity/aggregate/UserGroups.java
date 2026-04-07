package com.predictorama.backend.domain.entity.aggregate;

import com.predictorama.backend.domain.entity.Group;
import com.predictorama.backend.domain.entity.GroupMember;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserGroups {
    private final Group group;
    private final GroupMember membership;
}

