package com.predictorama.backend.domain.service;

import com.predictorama.backend.domain.entity.Team;
import com.predictorama.backend.domain.port.persistence.TeamRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamSyncService {

    private static final Logger log = LoggerFactory.getLogger(TeamSyncService.class);

    private final TeamRepositoryPort teamRepositoryPort;

    public Team saveOrGetTeam(Team incomingTeam) {
        if (incomingTeam == null || isBlank(incomingTeam.getExternalId())) {
            throw new IllegalArgumentException("Cannot save team with missing externalId");
        }

        if (isBlank(incomingTeam.getName())) {
            throw new IllegalArgumentException("Cannot save team with missing name");
        }

        return teamRepositoryPort.findByExternalId(incomingTeam.getExternalId())
                .map(existingTeam -> {
                    boolean teamChanged =
                            !Objects.equals(existingTeam.getName(), incomingTeam.getName()) ||
                            (incomingTeam.getImageUrl() != null &&
                                    !Objects.equals(existingTeam.getImageUrl(), incomingTeam.getImageUrl()));

                    if (!teamChanged) {
                        return existingTeam;
                    }

                    Team updatedTeam = Team.builder()
                            .id(existingTeam.getId())
                            .externalId(existingTeam.getExternalId())
                            .name(incomingTeam.getName())
                            .imageUrl(incomingTeam.getImageUrl())
                            .build();

                    Team savedTeam = teamRepositoryPort.save(updatedTeam);
                    log.info(
                            "Updated team in DB externalId={} name={} id={}",
                            savedTeam.getExternalId(),
                            savedTeam.getName(),
                            savedTeam.getId()
                    );
                    return savedTeam;
                })
                .orElseGet(() -> {
                    Team savedTeam = teamRepositoryPort.save(
                            Team.builder()
                                    .id(UUID.randomUUID())
                                    .externalId(incomingTeam.getExternalId())
                                    .name(incomingTeam.getName())
                                    .imageUrl(incomingTeam.getImageUrl())
                                    .build()
                    );

                    log.info(
                            "Created team in DB externalId={} name={} id={}",
                            savedTeam.getExternalId(),
                            savedTeam.getName(),
                            savedTeam.getId()
                    );
                    return savedTeam;
                });
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
