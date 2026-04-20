package com.predictorama.backend.domain.entity.aggregate;

import com.predictorama.backend.domain.entity.Group;
import com.predictorama.backend.domain.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GroupDetailsView {
    private Group group;
    private Role currentUserRole;
}
