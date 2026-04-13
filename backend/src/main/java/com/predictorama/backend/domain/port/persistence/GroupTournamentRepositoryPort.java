package com.predictorama.backend.domain.port.persistence;

import java.util.List;
import java.util.UUID;

public interface GroupTournamentRepositoryPort {
    List<UUID> findTournamentIdsByGroupId(UUID groupId);

    boolean existsByGroupIdAndTournamentId(UUID groupId, UUID tournamentId);

    void save(UUID groupId, UUID tournamentId);
}
