package com.predictorama.backend.adapter.persistence.mapper;

import com.predictorama.backend.adapter.persistence.entity.TournamentEntity;
import com.predictorama.backend.domain.entity.Tournament;

public class TournamentMapper {

    public static Tournament toDomain(TournamentEntity entity) {
        return Tournament.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .sport(entity.getSport())
                .build();
    }

    public static TournamentEntity toEntity(Tournament tournament) {
        return TournamentEntity.builder()
                .id(tournament.getId())
                .name(tournament.getName())
                .description(tournament.getDescription())
                .sport(tournament.getSport())
                .build();
    }
}
