package com.predictorama.backend.adapter.rest.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AddGroupTournamentRequest {
    @NotNull
    private UUID tournamentId;
}
