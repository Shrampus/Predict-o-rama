package com.predictorama.backend.adapter.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class GroupTournamentId implements Serializable {
    private UUID groupId;
    private UUID tournamentId;
}
