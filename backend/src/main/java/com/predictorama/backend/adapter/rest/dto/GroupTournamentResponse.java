package com.predictorama.backend.adapter.rest.dto;

import com.predictorama.backend.domain.entity.Tournament;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class GroupTournamentResponse {
    private UUID id;
    private String name;
    private String description;
    private Tournament.Sport sport;
}
