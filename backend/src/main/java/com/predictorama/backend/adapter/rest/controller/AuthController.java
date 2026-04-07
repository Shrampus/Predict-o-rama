package com.predictorama.backend.adapter.rest.controller;

import com.predictorama.backend.adapter.rest.SessionService;
import com.predictorama.backend.adapter.rest.dto.LoginRequest;
import com.predictorama.backend.adapter.rest.dto.UserResponseDto;
import com.predictorama.backend.adapter.rest.mapper.UserRestMapper;
import com.predictorama.backend.domain.service.AuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final SessionService sessionService;

    @PostMapping("/login")
    public UserResponseDto login(@RequestBody LoginRequest request, HttpSession session) {
        log.info("POST /api/auth/login - email={}", request.email());
        var user = authService.login(request.email(), request.password());
        sessionService.setUserId(session, user.getId());
        log.info("Login successful - userId={}", user.getId());
        return UserRestMapper.toResponse(user);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> me(HttpSession session) {
        return sessionService.getUserId(session)
                .map(userId -> {
                    var user = authService.getById(userId);
                    return ResponseEntity.ok(UserRestMapper.toResponse(user));
                })
                .orElse(ResponseEntity.status(401).build());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        sessionService.invalidate(session);
        return ResponseEntity.noContent().build();
    }
}
