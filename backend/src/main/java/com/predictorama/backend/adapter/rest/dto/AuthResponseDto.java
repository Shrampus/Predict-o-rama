package com.predictorama.backend.adapter.rest.dto;

public record AuthResponseDto( String token, String status, UserResponseDto user ) {}
