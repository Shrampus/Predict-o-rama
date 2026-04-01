package com.predictorama.backend.adapter.rest.mapper;

import com.predictorama.backend.adapter.rest.dto.UserResponse;
import com.predictorama.backend.domain.entity.User;

public class UserRestMapper {

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getSystemRole()
        );
    }
}
