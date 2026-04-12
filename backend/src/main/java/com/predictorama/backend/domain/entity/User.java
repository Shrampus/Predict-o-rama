package com.predictorama.backend.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class User {
    private UUID id;
    private String username;
    private String email;
    private Role systemRole;
    private String passwordHash;
}
