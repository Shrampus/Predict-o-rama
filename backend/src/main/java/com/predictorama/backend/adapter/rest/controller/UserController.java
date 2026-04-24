package com.predictorama.backend.adapter.rest.controller;

import com.predictorama.backend.adapter.rest.dto.CreateUserRequestDto;
import com.predictorama.backend.adapter.rest.dto.UserResponseDto;
import com.predictorama.backend.adapter.rest.mapper.UserRestMapper;
import com.predictorama.backend.domain.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping({"/api/users", "/api/v1/users"})
@RequiredArgsConstructor
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDto createUser(@RequestBody CreateUserRequestDto request, HttpServletRequest httpRequest) {
        log.info("POST {} - username={}", httpRequest.getRequestURI(), request.getUsername());
        UserResponseDto response = UserRestMapper.toResponse(userService.createUser(request.getUsername(), request.getEmail()));
        log.info("User created - id={}", response.getId());
        return response;
    }
}
