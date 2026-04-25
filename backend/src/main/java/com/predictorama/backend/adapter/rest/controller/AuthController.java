package com.predictorama.backend.adapter.rest.controller;

import com.predictorama.backend.adapter.rest.dto.*;
import com.predictorama.backend.adapter.rest.mapper.UserRestMapper;
import com.predictorama.backend.config.AuthUtils;
import com.predictorama.backend.config.JwtService;
import com.predictorama.backend.domain.entity.User;
import com.predictorama.backend.domain.service.AuthResult;
import com.predictorama.backend.domain.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/login")
    public AuthResponseDto login(@RequestBody LoginRequestDto loginRequest, HttpServletRequest request) {
        log.info("POST {} - email={}", request.getRequestURI(), loginRequest.email());
        AuthResult result = authService.login(loginRequest.email(), loginRequest.password());
        return toAuthResponse(result);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> me() {
        var userId = AuthUtils.currentUserId();
        var user = authService.getById(userId);
        return ResponseEntity.ok(UserRestMapper.toResponse(user));
    }

    @PostMapping("/google")
    public AuthResponseDto googleLogin(@RequestBody GoogleLoginRequestDto request) {
        AuthResult result = authService.loginWithGoogle(request.idToken());
        return toAuthResponse(result);
    }

    @PostMapping("/complete-profile")
    public ResponseEntity<AuthResponseDto> completeProfile(@RequestBody CompleteProfileRequestDto request) {
        var userId = AuthUtils.currentUserId();
        User user = authService.completeProfile(userId, request.username());
        String newAuthToken = jwtService.generateToken(userId, false);

        return ResponseEntity.ok(new AuthResponseDto(newAuthToken, "OK", UserRestMapper.toResponse(user)));
    }

    private String onboardingStatusMapper(boolean needsOnboarding) {
        return needsOnboarding ? "NEEDS_ONBOARDING" : "OK";
    }

    private AuthResponseDto toAuthResponse(AuthResult result) {
        String token = jwtService.generateToken(result.user().getId(), result.needsOnboarding());
        return new AuthResponseDto(
                token,
                onboardingStatusMapper(result.needsOnboarding()),
                UserRestMapper.toResponse(result.user())
        );
    }
}
