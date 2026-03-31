package com.predictorama.backend.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class PredictionPageResponseDto {
    private List<PredictionPageMatchDto> matches;
}