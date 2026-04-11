package com.predictorama.backend.adapter.rest.mapper;

import com.predictorama.backend.adapter.rest.dto.UserResponseDto;
import com.predictorama.backend.domain.entity.User;

public class UserRestMapper {

    public static UserResponseDto toResponse(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getSystemRole()
        );
    }
}
