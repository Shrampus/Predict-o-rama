package com.predictorama.backend.adapter.rest.dto;

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