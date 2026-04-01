package com.predictorama.backend.adapter.rest.dto;

import com.predictorama.backend.domain.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String username;
    private String email;
    private Role systemRole;
}
