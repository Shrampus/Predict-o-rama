package com.predictorama.backend.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class Tournament {
    private UUID id;
    private String name;
    private String description;
    private Sport sport;

    private enum Sport{
        FOOTBALL
    }
}
