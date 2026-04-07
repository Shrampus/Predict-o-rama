package com.predictorama.backend.adapter.rest.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class JoinGroupRequestDto {
    private UUID inviteCode;
}
