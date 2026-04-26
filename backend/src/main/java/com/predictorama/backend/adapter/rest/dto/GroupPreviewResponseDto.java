package com.predictorama.backend.adapter.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GroupPreviewResponseDto {
    private String name;
    private String adminName;
    private List<String> tournamentNames;
}
