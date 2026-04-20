package com.predictorama.backend.adapter.rest.controller;

import com.predictorama.backend.adapter.rest.dto.*;
import com.predictorama.backend.adapter.rest.mapper.UserRestMapper;
import com.predictorama.backend.config.JwtService;
import com.predictorama.backend.domain.entity.User;
import com.predictorama.backend.domain.service.AuthResult;
import com.predictorama.backend.domain.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/login")
    public AuthResponseDto login(@RequestBody LoginRequestDto request) {
        log.info("POST /api/auth/login - email={}", request.email());
        AuthResult result = authService.login(request.email(), request.password());
        String authToken = jwtService.generateToken(result.user().getId(), result.needsOnboarding());
        return new AuthResponseDto(authToken, onboardingStatusMapper(result.needsOnboarding()), UserRestMapper.toResponse(result.user()));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> me() {
        UUID userId = UUID.fromString((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        var user = authService.getById(userId);
        return ResponseEntity.ok(UserRestMapper.toResponse(user));
    }

    @PostMapping("/google")
    public AuthResponseDto googleLogin(@RequestBody GoogleLoginRequestDto request) {
        AuthResult result = authService.loginWithGoogle(request.idToken());
        String authToken = jwtService.generateToken(result.user().getId(), result.needsOnboarding());
        return new AuthResponseDto(authToken, onboardingStatusMapper(result.needsOnboarding()), UserRestMapper.toResponse(result.user()));
    }

    @PostMapping("/complete-profile")
    public ResponseEntity<AuthResponseDto> completeProfile(@RequestBody CompleteProfileRequestDto request) {
        UUID userId = UUID.fromString((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        User user = authService.completeProfile(userId, request.username());
        String newAuthToken = jwtService.generateToken(userId, false);

        return ResponseEntity.ok(new AuthResponseDto(newAuthToken, "OK", UserRestMapper.toResponse(user)));
    }

    private String onboardingStatusMapper(boolean needsOnboarding){
        return needsOnboarding ? "NEEDS_ONBOARDING" : "OK";
    }
}
