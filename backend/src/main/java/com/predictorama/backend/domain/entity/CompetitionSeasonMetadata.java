package com.predictorama.backend.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CompetitionSeasonMetadata {
    private String seasonIdentifier;
    private String seasonLabel;
}
