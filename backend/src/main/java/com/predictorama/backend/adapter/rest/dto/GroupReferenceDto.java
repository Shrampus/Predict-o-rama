package com.predictorama.backend.adapter.rest.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupReferenceDto {
    private String groupId;
    private String groupName;
    private String competitionId;
}
