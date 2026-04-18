package com.predictorama.backend.adapter.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddGroupMemberRequest {
    @NotBlank
    @Email
    private String email;
}
