package com.predictorama.backend.adapter.persistence.mapper;

import com.predictorama.backend.adapter.persistence.entity.TeamEntity;
import com.predictorama.backend.domain.entity.Team;

public class TeamMapper {

    public static Team toDomain(TeamEntity entity) {
        return Team.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }

    public static TeamEntity toEntity(Team team) {
        return TeamEntity.builder()
                .id(team.getId())
                .name(team.getName())
                .build();
    }
}
