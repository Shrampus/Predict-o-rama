package com.predictorama.backend.adapter.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class GroupResponseDto {
    private UUID id;
    private UUID ownerId;
    private UUID inviteCode;
    private String name;
    private String description;
}
