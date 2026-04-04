package com.predictorama.backend.adapter.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class JoinGroupRequest {
    private UUID userId;
    private UUID inviteCode;
}
