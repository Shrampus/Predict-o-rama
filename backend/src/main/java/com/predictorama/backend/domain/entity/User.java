package com.predictorama.backend.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class User {
    private UUID id;
    private String username;
    private String email;
    private Role systemRole;
    private String passwordHash;
    private String googleId;
}
